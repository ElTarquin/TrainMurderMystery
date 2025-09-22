package dev.doctor4t.trainmurdermystery.block_entity;

import dev.doctor4t.trainmurdermystery.index.TMMBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlateBlockEntity extends BlockEntity {
    private final List<ItemStack> storedItems = new ArrayList<>();
    public List<ItemStack> getStoredItems() {
        return storedItems;
    }
    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return;

        storedItems.add(stack.copy());
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    private int poisonedItemsCount = 0;

    public int getPoisonedItemsCount() {
        return poisonedItemsCount;
    }

    public void setPoisonedItemsCount(int poisonedItemsCount) {
        this.poisonedItemsCount = poisonedItemsCount;
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public PlateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static PlateBlockEntity create(BlockPos pos, BlockState state) {
        return new PlateBlockEntity(TMMBlockEntities.PLATE, pos, state);
    }

    public static <T extends BlockEntity> void clientTick(World world, BlockPos pos, BlockState state, T t) {
        PlateBlockEntity entity = (PlateBlockEntity) t;
        //if (!TMMClient.isHitman()) return;
        if (entity.getPoisonedItemsCount() == 0 || entity.getStoredItems().isEmpty()) return;
        if (Random.createThreadSafe().nextBetween(0, 20) < 17) return;

        world.addParticle(
                ParticleTypes.RAID_OMEN,
                pos.getX() + 0.5f,
                pos.getY(),
                pos.getZ() + 0.5f,
                0f, -0.75f, 0f
        );
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        NbtCompound itemsNbt = new NbtCompound();
        for (int i = 0; i < storedItems.size(); i++) {
            if (!storedItems.get(i).isEmpty())
                itemsNbt.put("Item" + i, storedItems.get(i).encode(registryLookup));
        }
        nbt.put("Items", itemsNbt);

        nbt.putInt("poisonedItemsCount", this.poisonedItemsCount);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        storedItems.clear();
        if (nbt.contains("Items")) {
            NbtCompound itemsNbt = nbt.getCompound("Items");
            for (String key : itemsNbt.getKeys()) {
                Optional<ItemStack> itemStack = ItemStack.fromNbt(registryLookup, itemsNbt.get(key));
                itemStack.ifPresent(storedItems::add);
            }
        }

        this.poisonedItemsCount = nbt.getInt("poisonedItemsCount");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
