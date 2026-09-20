package net.mehvahdjukaar.heraldics.common.blocks;

import net.mehvahdjukaar.heraldics.HeraldicsMod;
import net.mehvahdjukaar.heraldics.common.misc.PortcullisMover;
import net.mehvahdjukaar.heraldics.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortcullisBlock extends RotatedPillarBlock implements IDirectionalStickyBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape X_SHAPE = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape Y_SHAPE = Block.box(0, 6, 0, 16, 10, 16);
    private static final VoxelShape Z_SHAPE = Block.box(0, 0, 6, 16, 16, 10);

    public PortcullisBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
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
        boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(AXIS, context.getHorizontalDirection().getAxis())
                .setValue(WATERLOGGED, inWater);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
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
        if (state.getValue(AXIS) == Direction.Axis.Y) return InteractionResult.PASS;

        boolean leaveItToTheWinch = HeraldicsMod.SUPP && !CommonConfigs.ALWAYS_HAND_PUSHABLE.get();
        if (leaveItToTheWinch) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction dir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
        if (!PortcullisMover.canMove(level, pos, dir)) return InteractionResult.FAIL;

        //moving piston block entities are never synced, so like a piston the move runs on both sides from the event
        level.blockEvent(pos, this, dir.get3DDataValue(), 0);
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Direction dir = Direction.from3DDataValue(id);
        if (!PortcullisMover.move(level, pos, dir)) return false;

        float basePitch = dir == Direction.DOWN ? 0.9f : 1.05f;
        float pitch = basePitch + level.getRandom().nextFloat() * 0.12f - 0.06f;
        level.playSound(null, pos, HeraldicsMod.PORTCULLIS_MOVE.get(), SoundSource.BLOCKS, 0.8f, pitch);
        return true;
    }
}
