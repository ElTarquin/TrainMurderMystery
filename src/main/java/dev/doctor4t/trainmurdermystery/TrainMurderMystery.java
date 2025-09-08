package dev.doctor4t.trainmurdermystery;

import dev.doctor4t.trainmurdermystery.command.GiveRoomKeyCommand;
import dev.doctor4t.trainmurdermystery.index.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainMurderMystery implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Registry initializers
        TrainMurderMysterySounds.initialize();
        TrainMurderMysteryEntities.initialize();
        TrainMurderMysteryBlocks.initialize();
        TrainMurderMysteryItems.initialize();
        TrainMurderMysteryBlockEntities.initialize();
        TrainMurderMysteryParticles.initialize();

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
        }));

    }
}