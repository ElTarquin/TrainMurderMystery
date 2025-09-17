package dev.doctor4t.trainmurdermystery.index;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public interface TMMDataComponentTypes {
    ComponentType<Integer> BULLETS = register("bullets",
            builder -> builder.codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT)
    );

    static void initialize() {
    }

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, TMM.id(name), builderOperator.apply(ComponentType.builder()).build());
    }
}
