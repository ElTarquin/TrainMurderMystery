package dev.doctor4t.trainmurdermystery.block;

import dev.doctor4t.trainmurdermystery.index.TrainMurderMysteryProperties;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public abstract class TrainMurderMysteryButtonBlock extends ButtonBlock {
    public static final BooleanProperty ACTIVE = TrainMurderMysteryProperties.ACTIVE;

    public TrainMurderMysteryButtonBlock(Settings settings) {
        super(BlockSetType.IRON, 20, settings);
        this.setDefaultState(super.getDefaultState().with(ACTIVE, true));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState != null) {
            return placementState.with(ACTIVE, true);
        }
        return null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) && state.get(ACTIVE) ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) && state.get(ACTIVE) && getDirection(state) == direction ? 15 : 0;
    }

    @Override
    public void powerOn(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        super.powerOn(state, world, pos, player);
    }

    @Override
    protected void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(player, pos, this.getClickSound(powered), SoundCategory.BLOCKS, 0.5f, powered ? 1.0f : 1.5f);
    }

    @Override
    protected SoundEvent getClickSound(boolean powered) {
        return TMMSounds.BLOCK_SPACE_BUTTON_TOGGLE;
    }
}
