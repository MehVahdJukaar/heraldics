package net.mehvahdjukaar.feudalist.common.blocks;

import net.mehvahdjukaar.feudalist.FeudalistMod;
import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final int MAX_MOVED_BY_HAND = 64;

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
    public boolean canStickTo(BlockState state, Direction face, BlockState neighbor) {
        Direction.Axis axis = state.getValue(AXIS);
        return face.getAxis() != axis
                && neighbor.getBlock() instanceof PortcullisBlock
                && neighbor.getValue(AXIS) == axis;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AXIS) == Direction.Axis.Y || FeudalistMod.SUPP) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        Direction dir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
        return moveGrate(level, pos, dir) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    private static boolean moveGrate(Level level, BlockPos origin, Direction dir) {
        Set<BlockPos> grate = collectGrate(level, origin);
        if (grate == null) return false;

        for (BlockPos pos : grate) {
            BlockPos to = pos.relative(dir);
            if (!grate.contains(to) && !level.getBlockState(to).canBeReplaced()) return false;
        }

        Map<BlockPos, BlockState> moved = new HashMap<>();
        for (BlockPos pos : grate) {
            moved.put(pos.relative(dir), level.getBlockState(pos));
        }
        int clearFlags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
        for (BlockPos pos : grate) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), clearFlags);
        }
        moved.forEach((pos, state) -> level.setBlock(pos, state, Block.UPDATE_ALL));
        return true;
    }

    /**
     * Flood fill over every panel that a piston would carry along. Null if the thing is too big.
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
                if (found.size() >= MAX_MOVED_BY_HAND) return null;
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
