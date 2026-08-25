package net.mehvahdjukaar.feudalist.common.blocks;

import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Grate panel that pistons drag around as one piece. AXIS is the panel normal, so the four faces
 * that fill a whole block side can stick, and only to another panel lying the same way.
 */
public class PortcullisBlock extends RotatedPillarBlock implements IDirectionalStickyBlock {

    private static final VoxelShape X_SHAPE = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape Y_SHAPE = Block.box(0, 6, 0, 16, 10, 16);
    private static final VoxelShape Z_SHAPE = Block.box(0, 0, 6, 16, 16, 10);

    private static final int MAX_SEARCHED = 64;

    public PortcullisBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public boolean canStickTo(BlockState state, Direction face, BlockState neighbor) {
        Direction.Axis axis = state.getValue(AXIS);
        return face.getAxis() != axis
                && neighbor.getBlock() instanceof PortcullisBlock
                && neighbor.getValue(AXIS) == axis;
    }

    //placeholder until the supplementaries winch is there to raise these properly
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AXIS) == Direction.Axis.Y || PlatHelper.isModLoaded("supplementaries")) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        boolean lowering = player.isShiftKeyDown();
        Direction dir = lowering ? Direction.DOWN : Direction.UP;
        if (!moveGrate(level, pos, dir)) return InteractionResult.FAIL;

        level.playSound(null, pos, lowering ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND,
                SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.25f + 0.6f);
        return InteractionResult.CONSUME;
    }

    /**
     * Same handoff a piston does, just with no piston: the structure is resolved as if one sat right
     * behind the grate, then every block becomes a moving piston block entity that slides on its own.
     */
    private static boolean moveGrate(Level level, BlockPos clicked, Direction dir) {
        BlockPos rear = findRearBlock(level, clicked, dir);
        if (rear == null) return false;

        PistonStructureResolver resolver = new PistonStructureResolver(level, rear.relative(dir.getOpposite()), dir, true);
        if (!resolver.resolve()) return false;

        List<BlockPos> toPush = resolver.getToPush();
        List<BlockPos> toDestroy = resolver.getToDestroy();

        Map<BlockPos, BlockState> leftBehind = new HashMap<>();
        for (BlockPos pos : toPush) {
            leftBehind.put(pos, level.getBlockState(pos));
        }

        BlockState[] oldStates = new BlockState[toPush.size() + toDestroy.size()];
        int i = 0;

        for (int j = toDestroy.size() - 1; j >= 0; --j) {
            BlockPos pos = toDestroy.get(j);
            BlockState destroyed = level.getBlockState(pos);
            BlockEntity tile = destroyed.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            dropResources(destroyed, level, pos, tile);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            if (!destroyed.is(BlockTags.FIRE)) {
                level.addDestroyBlockEffect(pos, destroyed);
            }
            oldStates[i++] = destroyed;
        }

        for (int j = toPush.size() - 1; j >= 0; --j) {
            BlockPos from = toPush.get(j);
            BlockState moved = level.getBlockState(from);
            BlockPos to = from.relative(dir);
            leftBehind.remove(to);
            BlockState movingState = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, dir);
            level.setBlock(to, movingState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_CLIENTS);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(to, movingState, moved, dir, true, false));
            oldStates[i++] = moved;
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : leftBehind.keySet()) {
            level.setBlock(pos, air, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
        leftBehind.forEach((pos, old) -> {
            old.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            air.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            air.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
        });

        i = 0;
        for (int j = toDestroy.size() - 1; j >= 0; --j) {
            BlockState old = oldStates[i++];
            BlockPos pos = toDestroy.get(j);
            old.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos, old.getBlock());
        }
        for (int j = toPush.size() - 1; j >= 0; --j) {
            level.updateNeighborsAt(toPush.get(j), oldStates[i++].getBlock());
        }
        return true;
    }

    /**
     * Furthest panel opposite to the travel direction. The fake piston goes one past it, so it can
     * never end up sitting inside the grate it is meant to shove.
     */
    private static BlockPos findRearBlock(Level level, BlockPos origin, Direction dir) {
        Set<BlockPos> grate = collectGrate(level, origin);
        if (grate == null) return null;
        int step = dir.getAxisDirection().getStep();
        return grate.stream().min(Comparator.comparingInt(p -> p.get(dir.getAxis()) * step)).orElse(null);
    }

    /**
     * Flood fill over every panel that sticks to the clicked one. Null if the thing is too big.
     */
    private static Set<BlockPos> collectGrate(Level level, BlockPos origin) {
        Set<BlockPos> found = new HashSet<>();
        Deque<BlockPos> toVisit = new ArrayDeque<>();
        found.add(origin);
        toVisit.add(origin);
        while (!toVisit.isEmpty()) {
            BlockPos pos = toVisit.removeFirst();
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (found.contains(next)) continue;
                BlockState neighbor = level.getBlockState(next);
                if (!sticksTogether(state, dir, neighbor)) continue;
                if (found.size() >= MAX_SEARCHED) return null;
                found.add(next);
                toVisit.add(next);
            }
        }
        return found;
    }

    private static boolean sticksTogether(BlockState state, Direction face, BlockState neighbor) {
        return state.getBlock() instanceof IDirectionalStickyBlock a && a.canStickTo(state, face, neighbor)
                && neighbor.getBlock() instanceof IDirectionalStickyBlock b && b.canStickTo(neighbor, face.getOpposite(), state);
    }
}
