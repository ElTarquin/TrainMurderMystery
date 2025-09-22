package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerStoreComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;

public record StoreBuyPayload(int index) implements CustomPayload {
	public static final Id<StoreBuyPayload> ID = new Id<>(TMM.id("storebuy"));
    public static final PacketCodec<PacketByteBuf, StoreBuyPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, StoreBuyPayload::index, StoreBuyPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<StoreBuyPayload> {
		@Override
		public void receive(@NotNull StoreBuyPayload payload, ServerPlayNetworking.@NotNull Context context) {
			PlayerStoreComponent.KEY.get(context.player()).tryBuy(payload.index());
		}
	}
}