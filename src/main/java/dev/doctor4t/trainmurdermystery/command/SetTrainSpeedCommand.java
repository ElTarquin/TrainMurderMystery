package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SetTrainSpeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:setTrainSpeed")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(
                                CommandManager.argument("speed", IntegerArgumentType.integer())
                                        .executes(context -> setTrainSpeed(context.getSource(), IntegerArgumentType.getInteger(context, "speed")))
                        )
        );
    }

    private static int setTrainSpeed(ServerCommandSource source, int speed) {
        TMMComponents.TRAIN.get(source.getWorld()).setTrainSpeed(speed);
        return 1;
    }
}
