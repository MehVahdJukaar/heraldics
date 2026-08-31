package net.mehvahdjukaar.heraldics.common.blocks;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.common.misc.PortcullisMover;
import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortcullisBlock extends RotatedPillarBlock implements IDirectionalStickyBlock {

    private static final VoxelShape X_SHAPE = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape Y_SHAPE = Block.box(0, 6, 0, 16, 10, 16);
    private static final VoxelShape Z_SHAPE = Block.box(0, 0, 6, 16, 16, 10);

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AXIS) == Direction.Axis.Y || HeraldicsMod.SUPP) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Direction dir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
        if (!PortcullisMover.canMove(level, pos, dir)) return InteractionResult.FAIL;

        level.blockEvent(pos, this, dir.get3DDataValue(), 0);
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Direction dir = Direction.from3DDataValue(id);
        if (!PortcullisMover.move(level, pos, dir)) return false;

        level.playSound(null, pos, dir == Direction.DOWN ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND,
                SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.25f + 0.6f);
        return true;
    }
}
