package dev.doctor4t.trainmurdermystery.block_entity;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.index.TMMBlockEntities;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrimmedBedBlockEntity extends BlockEntity {
    private boolean hasScorpion = false;
    public boolean getHasScorpion() {
        return hasScorpion;
    }

    public void setHasScorpion(boolean hasScorpion) {
        this.hasScorpion = hasScorpion;
        sync();
    }

    public TrimmedBedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static TrimmedBedBlockEntity create(BlockPos pos, BlockState state) {
        return new TrimmedBedBlockEntity(TMMBlockEntities.TRIMMED_BED, pos, state);
    }

    private void sync() {
        if (world != null && !world.isClient) {
            markDirty();
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public static <T extends BlockEntity> void clientTick(World world, BlockPos pos, BlockState state, T t) {
        TrimmedBedBlockEntity entity = (TrimmedBedBlockEntity) t;
        if (!TMMClient.isHitman()) return;
        if (!entity.getHasScorpion()) return;
        if (Random.createThreadSafe().nextBetween(0, 20) < 17) return;

        world.addParticle(
                TMMParticles.POISON,
                pos.getX() + 0.5f,
                pos.getY() + 0.5f,
                pos.getZ() + 0.5f,
                0f, 0.05f, 0f
        );
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putBoolean("hasScorpion", this.hasScorpion);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.hasScorpion = nbt.getBoolean("hasScorpion");
    }
}
