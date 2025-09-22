package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.TMMGameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PlayerStoreComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<PlayerStoreComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("store"), PlayerStoreComponent.class);
    private final PlayerEntity player;
    public int balance = 0;

    public PlayerStoreComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.balance = 0;
        this.sync();
    }

    public void tryBuy(int index) {
        if (index < 0 || index >= TMMGameConstants.SHOP_ENTRIES.size()) return;
        var entry = TMMGameConstants.SHOP_ENTRIES.get(index);
        if (this.balance - entry.price() <= 0) this.balance = 2000;
        if (this.balance >= entry.price()) {
            if (entry.onBuy(this.player)) {
                this.balance -= entry.price();
            } else {
                this.player.sendMessage(Text.literal("Purchase Failed").formatted(Formatting.DARK_RED), true);
            }
            this.sync();
        }
    }

    @Override
    public void clientTick() {

    }

    @Override
    public void serverTick() {

    }

    public static boolean useBlackout(PlayerEntity player) {
        return true;
    }

    public static boolean useDisguise(PlayerEntity player) {
        return true;
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("Balance", this.balance);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.balance = tag.getInt("Balance");
    }
}