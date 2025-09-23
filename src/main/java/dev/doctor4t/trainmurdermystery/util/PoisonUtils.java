package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block_entity.TrimmedBedBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BedBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PoisonUtils {
    public static float getFovMultiplier(float tickDelta, PlayerPoisonComponent poisonComponent) {
        if (!poisonComponent.pulsing) return 1f;

        poisonComponent.pulseProgress += tickDelta * 0.1f;

        if (poisonComponent.pulseProgress >= 1f) {
            poisonComponent.pulsing = false;
            poisonComponent.pulseProgress = 0f;
            return 1f;
        }

        float maxAmplitude = 0.1f;
        float minAmplitude = 0.025f;

        float result = getResult(poisonComponent, minAmplitude, maxAmplitude);

        return result;
    }

    private static float getResult(PlayerPoisonComponent poisonComponent, float minAmplitude, float maxAmplitude) {
        float amplitude = minAmplitude + (maxAmplitude - minAmplitude) * (1f - ((float) poisonComponent.poisonTicks / 1200f));

        float result;

        if (poisonComponent.pulseProgress < 0.25f) {
            result = 1f - amplitude * (float) Math.sin(Math.PI * (poisonComponent.pulseProgress / 0.25f));
        } else if (poisonComponent.pulseProgress < 0.5f) {
            result = 1f - amplitude * (float) Math.sin(Math.PI * ((poisonComponent.pulseProgress - 0.25f) / 0.25f));
        } else {
            result = 1f;
        }
        return result;
    }

    public static void bedPoison(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        BlockPos bedPos = player.getBlockPos();

        TrimmedBedBlockEntity blockEntity = findHeadInBox(world, bedPos);
        if (blockEntity == null) return;

        if (!world.isClient) {
            blockEntity.setHasScorpion(false);
            int poisonTicks = PlayerPoisonComponent.KEY.get(player).poisonTicks;

            if (poisonTicks == -1) PlayerPoisonComponent.KEY.get(player).setPoisonTicks(
                    Random.createThreadSafe().nextBetween(PlayerPoisonComponent.clampTime.getLeft(), PlayerPoisonComponent.clampTime.getRight()));
            else PlayerPoisonComponent.KEY.get(player).setPoisonTicks(MathHelper.clamp(
                    poisonTicks - Random.createThreadSafe().nextBetween(100, 300), 0, PlayerPoisonComponent.clampTime.getRight()));

            ServerPlayNetworking.send(
                    player, new PoisonOverlayPayload("game.player.poisoned")
            );
        }
    }

    private static TrimmedBedBlockEntity findHeadInBox(World world, BlockPos centerPos) {
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centerPos.add(dx, dy, dz);
                    TrimmedBedBlockEntity entity = resolveHead(world, pos);
                    if (entity != null && entity.getHasScorpion()) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolve a bed block (head or foot) into its head entity.
     */
    private static TrimmedBedBlockEntity resolveHead(World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof TrimmedBedBlockEntity entity)) return null;

        BedPart part = world.getBlockState(pos).get(BedBlock.PART);
        Direction facing = world.getBlockState(pos).get(HorizontalFacingBlock.FACING);

        if (part == BedPart.HEAD) return entity;

        if (part == BedPart.FOOT) {
            BlockPos headPos = pos.offset(facing);
            if (world.getBlockEntity(headPos) instanceof TrimmedBedBlockEntity headEntity &&
                    world.getBlockState(headPos).get(BedBlock.PART) == BedPart.HEAD) return headEntity;
        }

        return null;
    }


    public record PoisonOverlayPayload(String translationKey) implements CustomPayload {
        public static final Id<PoisonOverlayPayload> ID =
                new Id<>(TMM.id("poisoned_text"));

        public static final PacketCodec<RegistryByteBuf, PoisonOverlayPayload> CODEC =
                PacketCodec.of(PoisonOverlayPayload::write, PoisonOverlayPayload::read);

        private void write(RegistryByteBuf buf) {
            buf.writeString(translationKey);
        }

        private static PoisonOverlayPayload read(RegistryByteBuf buf) {
            return new PoisonOverlayPayload(buf.readString());
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<PoisonOverlayPayload> {
            @Override
            public void receive(@NotNull PoisonOverlayPayload payload, ClientPlayNetworking.@NotNull Context context) {
                var client = MinecraftClient.getInstance();
                client.execute(() -> client.inGameHud.setOverlayMessage(Text.translatable(payload.translationKey()), false));
            }
        }
    }
}