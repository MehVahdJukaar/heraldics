package net.mehvahdjukaar.feudalist.common.misc;

import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Slides a whole grate one block like a piston would, with no piston. Vanilla's resolver caps a push
 * at 12 blocks grate included, too few for a real portcullis, so this one walks the grate itself and
 * only borrows the piston rules for what sits in front of it.
 * Placeholder until the supplementaries winch moves these.
 */
public class PortcullisMover {

    //grate plus whatever it shoves
    public static final int MAX_MOVED_BLOCKS = 64;

    private final Level level;
    private final Direction pushDirection;
    private final List<BlockPos> toPush = new ArrayList<>();
    private final List<BlockPos> toDestroy = new ArrayList<>();

    private PortcullisMover(Level level, Direction pushDirection) {
        this.level = level;
        this.pushDirection = pushDirection;
    }

    public static boolean canMove(Level level, BlockPos gratePos, Direction dir) {
        return resolve(level, gratePos, dir) != null;
    }

    /**
     * Moves the grate that contains gratePos. Nothing changes when it returns false.
     * Run it on both sides, the moving piston block entities it makes are never synced.
     */
    public static boolean move(Level level, BlockPos gratePos, Direction dir) {
        PortcullisMover mover = resolve(level, gratePos, dir);
        if (mover == null) return false;
        mover.moveBlocks();
        return true;
    }

    @Nullable
    private static PortcullisMover resolve(Level level, BlockPos gratePos, Direction dir) {
        Set<BlockPos> grate = collectGrate(level, gratePos);
        if (grate == null) return null;
        PortcullisMover mover = new PortcullisMover(level, dir);
        return mover.resolve(grate) ? mover : null;
    }

    /**
     * Flood fill over every block that sticks to the clicked one. Null if the thing is too big.
     */
    @Nullable
    private static Set<BlockPos> collectGrate(Level level, BlockPos origin) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Deque<BlockPos> toVisit = new ArrayDeque<>();
        found.add(origin);
        toVisit.add(origin);
        while (!toVisit.isEmpty()) {
            BlockPos pos = toVisit.removeFirst();
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (found.contains(next)) continue;
                if (!sticksTogether(state, dir, level.getBlockState(next))) continue;
                if (found.size() >= MAX_MOVED_BLOCKS) return null;
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

    private boolean resolve(Set<BlockPos> grate) {
        for (BlockPos pos : grate) {
            if (!isPushable(pos, false)) return false;
        }
        toPush.addAll(grate);
        for (BlockPos pos : grate) {
            if (!addBlocksInFront(pos)) return false;
        }
        return true;
    }

    /**
     * Forward half of vanilla's addBlockLine. Pushed blocks are plain pushed, nothing glued to them
     * comes along.
     */
    private boolean addBlocksInFront(BlockPos origin) {
        BlockPos pos = origin;
        while (true) {
            pos = pos.relative(pushDirection);
            //that block's own line covers what is past it
            if (toPush.contains(pos)) return true;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) return true;
            if (!isPushable(pos, true)) return false;
            if (state.getPistonPushReaction() == PushReaction.DESTROY) {
                toDestroy.add(pos);
                return true;
            }
            if (toPush.size() >= MAX_MOVED_BLOCKS) return false;
            toPush.add(pos);
        }
    }

    private boolean isPushable(BlockPos pos, boolean allowDestroy) {
        return PistonBaseBlock.isPushable(level.getBlockState(pos), level, pos, pushDirection, allowDestroy, pushDirection);
    }

    /**
     * Same handoff a piston does: every block becomes a moving piston block entity that slides on its own.
     * States are read up front so the write order doesn't matter.
     */
    private void moveBlocks() {
        Map<BlockPos, BlockState> moved = new LinkedHashMap<>();
        for (BlockPos pos : toPush) {
            moved.put(pos, level.getBlockState(pos));
        }
        List<BlockState> destroyed = new ArrayList<>();
        for (BlockPos pos : toDestroy) {
            destroyed.add(level.getBlockState(pos));
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < toDestroy.size(); i++) {
            BlockPos pos = toDestroy.get(i);
            BlockState state = destroyed.get(i);
            BlockEntity tile = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(state, level, pos, tile);
            level.setBlock(pos, air, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            if (!state.is(BlockTags.FIRE)) {
                level.addDestroyBlockEffect(pos, state);
            }
        }

        Set<BlockPos> leftBehind = new LinkedHashSet<>(toPush);
        BlockState movingState = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, pushDirection);
        moved.forEach((from, state) -> {
            BlockPos to = from.relative(pushDirection);
            leftBehind.remove(to);
            level.setBlock(to, movingState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_CLIENTS);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(to, movingState, state, pushDirection, true, false));
        });

        for (BlockPos pos : leftBehind) {
            level.setBlock(pos, air, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
        for (BlockPos pos : leftBehind) {
            moved.get(pos).updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            air.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            air.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
        }

        for (int i = 0; i < toDestroy.size(); i++) {
            BlockPos pos = toDestroy.get(i);
            BlockState state = destroyed.get(i);
            state.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos, state.getBlock());
        }
        moved.forEach((pos, state) -> level.updateNeighborsAt(pos, state.getBlock()));
    }
}
