package dev.doctor4t.trainmurdermystery.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopEntry {
    private final ItemStack stack;
    private final int price;

    public ShopEntry(ItemStack stack, int price) {
        this.stack = stack;
        this.price = price;
    }

    public boolean onBuy(@NotNull PlayerEntity player) {
        for (var i = 0; i < 9; i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                player.getInventory().setStack(i, this.stack.copy());
                return true;
            }
        }
        return false;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public int price() {
        return this.price;
    }
}