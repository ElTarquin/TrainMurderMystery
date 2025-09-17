package dev.doctor4t.trainmurdermystery.client.gui.screen.ingame;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LimitedInventoryScreen extends LimitedHandledScreen<PlayerScreenHandler> {
    public static final Identifier BACKGROUND_TEXTURE = TMM.id("textures/gui/container/limited_inventory.png");

    public LimitedInventoryScreen(PlayerEntity player) {
        super(player.playerScreenHandler, player.getInventory(), Text.empty());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = this.y;
        context.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

}
