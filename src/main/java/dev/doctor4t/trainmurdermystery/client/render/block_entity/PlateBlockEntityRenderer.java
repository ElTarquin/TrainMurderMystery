package dev.doctor4t.trainmurdermystery.client.render.block_entity;

import dev.doctor4t.trainmurdermystery.block_entity.PlateBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class PlateBlockEntityRenderer implements BlockEntityRenderer<PlateBlockEntity> {
    private final ItemRenderer itemRenderer;

    public PlateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(PlateBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        int itemCount = entity.getStoredItems().size();
        if (itemCount == 0) return;

        double radius = 0.25;
        double centerX = 0.5;
        double centerY = 0.0375;
        double centerZ = 0.5;

        for (int i = 0; i < itemCount; i++) {
            ItemStack stack = entity.getStoredItems().get(i);
            if (stack == null) continue;

            double angle = (2 * Math.PI / itemCount) * i;

            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);

            matrices.push();

            matrices.translate(x, centerY, z);

            float rotationDegrees = (float) Math.toDegrees(angle) + 90f;

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(75f));
            matrices.scale(0.4f, 0.4f, 0.4f);

            itemRenderer.renderItem(stack,
                    net.minecraft.client.render.model.json.ModelTransformationMode.FIXED,
                    light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);

            matrices.pop();
        }
    }
}
