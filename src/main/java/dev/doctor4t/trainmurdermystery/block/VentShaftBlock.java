package dev.doctor4t.trainmurdermystery.block;

import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author EightSidedSquare
 */

public class VentShaftBlock extends Block {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    protected static final VoxelShape BASE_SHAPE = VoxelShapes.combineAndSimplify(
            VoxelShapes.fullCube(),
            Block.createCuboidShape(2, 2, 2, 14, 14, 14),
            BooleanBiFunction.ONLY_FIRST);
    protected static final VoxelShape NORTH_OPENING = Block.createCuboidShape(2, 2, 0, 14, 14, 2);
    protected static final VoxelShape EAST_OPENING = Block.createCuboidShape(14, 2, 2, 16, 14, 14);
    protected static final VoxelShape SOUTH_OPENING = Block.createCuboidShape(2, 2, 14, 14, 14, 16);
    protected static final VoxelShape WEST_OPENING = Block.createCuboidShape(0, 2, 2, 2, 14, 14);
    protected static final VoxelShape UP_OPENING = Block.createCuboidShape(2, 14, 2, 14, 16, 14);
    protected static final VoxelShape DOWN_OPENING = Block.createCuboidShape(2, 0, 2, 14, 2, 14);
    protected static final Function<BlockState, VoxelShape> STATE_TO_SHAPE = Util.memoize(VentShaftBlock::calculateShape);

    public VentShaftBlock(Settings settings) {
        super(settings);
        this.setDefaultState(super.getDefaultState()
                .with(NORTH, true)
                .with(EAST, true)
                .with(SOUTH, true)
                .with(WEST, true)
                .with(UP, true)
                .with(DOWN, true));
    }

    private static VoxelShape calculateShape(BlockState state) {
        VoxelShape shape = BASE_SHAPE;
        for (Direction direction : Direction.values())
            if (!state.get(ConnectingBlock.FACING_PROPERTIES.get(direction)))
                shape = VoxelShapes.combineAndSimplify(shape, VentShaftBlock.getOpeningShape(direction), BooleanBiFunction.ONLY_FIRST);
        return shape;
    }

    private static VoxelShape getOpeningShape(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN_OPENING;
            case UP -> UP_OPENING;
            case NORTH -> NORTH_OPENING;
            case SOUTH -> SOUTH_OPENING;
            case WEST -> WEST_OPENING;
            case EAST -> EAST_OPENING;
        };
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return STATE_TO_SHAPE.apply(state);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = this.getDefaultState();
        for (Direction direction : Direction.values()) {
            if (world.getBlockState(pos.offset(direction)).isOf(this))
                state = state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), false);
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (neighborState.isOf(this)) {
            return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), false);
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isEmpty()) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos blockPos, PlayerEntity player, BlockHitResult hit) {
        Vec3d centerPos = blockPos.toCenterPos();
        Vec3d pos = hit.getPos();
        if (!player.isCrawling()
                && MathHelper.fractionalPart(pos.getX()) != 0
                && MathHelper.fractionalPart(pos.getY()) != 0
                && MathHelper.fractionalPart(pos.getZ()) != 0) {
            player.setPose(EntityPose.SWIMMING);
            player.setOnGround(true);
            player.setPosition(centerPos.getX(), blockPos.getY() + 0.125f, centerPos.getZ());
            player.playSound(TMMSounds.VENT_SHAFT.getStepSound(), 1.0f, 1.0f);
            return ActionResult.success(world.isClient);
        }

        return super.onUse(state, world, blockPos, player, hit);
    }
}
