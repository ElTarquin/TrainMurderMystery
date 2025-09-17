package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.trainmurdermystery.game.TMMGameLoop;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class StartGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:startGame")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> startGame(context.getSource()))

        );
    }

    private static int startGame(ServerCommandSource source) {
        TMMGameLoop.startGame(source.getWorld());
        return 1;
    }
}
