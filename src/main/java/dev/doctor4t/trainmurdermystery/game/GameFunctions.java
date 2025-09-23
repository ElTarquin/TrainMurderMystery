package dev.doctor4t.trainmurdermystery.game;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.doctor4t.trainmurdermystery.cca.*;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class GameFunctions {
    public static void limitPlayerToBox(ServerPlayerEntity player, Box box) {
        Vec3d playerPos = player.getPos();

        if (!box.contains(playerPos)) {
            double x = playerPos.getX();
            double y = playerPos.getY();
            double z = playerPos.getZ();

            if (z < box.minZ) {
                z = box.minZ;
            }
            if (z > box.maxZ) {
                z = box.maxZ;
            }

            if (y < box.minY) {
                y = box.minY;
            }
            if (y > box.maxY) {
                y = box.maxY;
            }

            if (x < box.minX) {
                x = box.minX;
            }
            if (x > box.maxX) {
                x = box.maxX;
            }

            player.requestTeleport(x, y, z);
        }
    }

    public static void startGame(ServerWorld world) {
        GameWorldComponent component = TMMComponents.GAME.get(world);
        component.setGameStatus(GameWorldComponent.GameStatus.STARTING);
    }

    public static void stopGame(ServerWorld world) {
        GameWorldComponent component = TMMComponents.GAME.get(world);
        component.setGameStatus(GameWorldComponent.GameStatus.STOPPING);
    }

    public static void initializeGame(ServerWorld world) {
        TrainWorldComponent trainComponent = TMMComponents.TRAIN.get(world);
        trainComponent.setTrainSpeed(130);
        GameWorldComponent gameComponent = TMMComponents.GAME.get(world);

        world.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, world.getServer());
        world.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, world.getServer());
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
        world.getGameRules().get(GameRules.DO_MOB_GRIEFING).set(false, world.getServer());
        world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, world.getServer());
        world.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, world.getServer());
        world.getGameRules().get(GameRules.DO_TRADER_SPAWNING).set(false, world.getServer());
        world.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(9999, world.getServer());
        world.getServer().setDifficulty(Difficulty.PEACEFUL, true);
        world.setTimeOfDay(18000);

        // dismount all players as it can cause issues
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.dismountVehicle();
        }

        // teleport players to play area
        List<ServerPlayerEntity> playerPool = world.getPlayers(serverPlayerEntity -> !serverPlayerEntity.isInCreativeMode() && !serverPlayerEntity.isSpectator() && (TMMGameConstants.READY_AREA.contains(serverPlayerEntity.getPos())));
        for (ServerPlayerEntity player : playerPool) {
            Vec3d pos = player.getPos().add(Vec3d.of(TMMGameConstants.PLAY_POS.subtract(BlockPos.ofFloored(TMMGameConstants.READY_AREA.getMinPos()))));
            player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
        }

        // teleport non playing players
        for (ServerPlayerEntity player : world.getPlayers(serverPlayerEntity -> !playerPool.contains(serverPlayerEntity))) {
            player.changeGameMode(GameMode.SPECTATOR);
            TMMGameConstants.SPECTATOR_TP.accept(player);
        }

        // limit the game to 14 players, put players 15 to n in spectator mode
        Collections.shuffle(playerPool);
        while (playerPool.size() > 14) {
            playerPool.getFirst().changeGameMode(GameMode.SPECTATOR);
            playerPool.removeFirst();
        }

        List<ServerPlayerEntity> rolePlayerPool = new ArrayList<>(playerPool);

        // clear items, clear previous game data
        for (ServerPlayerEntity serverPlayerEntity : rolePlayerPool) {
            serverPlayerEntity.getInventory().clear();
            PlayerMoodComponent.KEY.get(serverPlayerEntity).reset();
            PlayerStoreComponent.KEY.get(serverPlayerEntity).reset();
            PlayerPoisonComponent.KEY.get(serverPlayerEntity).reset();
        }
        gameComponent.resetRoleLists();

        // select hitmen
        int hitmanCount = (int) Math.floor(rolePlayerPool.size() * .2f);
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < hitmanCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            rolePlayerPool.removeFirst();
            player.giveItemStack(new ItemStack(TMMItems.KNIFE));
            player.giveItemStack(new ItemStack(TMMItems.LOCKPICK));

            ItemStack letter = new ItemStack(TMMItems.LETTER);
            letter.set(DataComponentTypes.ITEM_NAME, Text.translatable(letter.getTranslationKey() + ".instructions"));
            player.giveItemStack(letter);

            gameComponent.addHitman(player);
        }

        // select detectives
        int detectiveCount = hitmanCount;
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < detectiveCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            rolePlayerPool.removeFirst();
            player.giveItemStack(new ItemStack(TMMItems.REVOLVER));
            player.giveItemStack(new ItemStack(TMMItems.BODY_BAG));

            ItemStack letter = new ItemStack(TMMItems.LETTER);
            letter.set(DataComponentTypes.ITEM_NAME, Text.translatable(letter.getTranslationKey() + ".notes"));
            player.giveItemStack(letter);

            gameComponent.addDetective(player);
        }

        // select targets
        int targetCount = rolePlayerPool.size() / 2;
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < targetCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            rolePlayerPool.removeFirst();
            gameComponent.addTarget(player);
        }

        // select rooms
        Collections.shuffle(playerPool);
        for (int i = 0; i < playerPool.size(); i++) {
            ItemStack itemStack = new ItemStack(TMMItems.KEY);
            int roomNumber = (int) Math.floor((double) (i + 2) / 2);
            itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> new LoreComponent(Text.literal("Room " + roomNumber).getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
            ServerPlayerEntity player = playerPool.get(i);
            player.giveItemStack(itemStack);

            // give pamphlet
            ItemStack letter = new ItemStack(TMMItems.LETTER);

            letter.set(DataComponentTypes.ITEM_NAME, Text.translatable(letter.getTranslationKey() + ".pamphlet"));
            int letterColor = 0xC5AE8B;
            String tipString = "tip.letter.pamphlet.";
            letter.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
                        List<Text> text = new ArrayList<>();
                        UnaryOperator<Style> stylizer = style -> style.withItalic(false).withColor(letterColor);
                        text.add(Text.translatable(tipString + "name", player.getName().getString()).styled(style -> style.withItalic(false).withColor(0xFFFFFF)));
                        text.add(Text.translatable(tipString + "room").styled(stylizer));
                        text.add(Text.translatable(tipString + "tooltip1",
                                Text.translatable(tipString + "room." + switch (roomNumber) {
                                    case 1 -> "grand_suite";
                                    case 2, 3 -> "cabin_suite";
                                    default -> "twin_cabin";
                                }).getString()
                        ).styled(stylizer));
                        text.add(Text.translatable(tipString + "tooltip2").styled(stylizer));

                        return new LoreComponent(text);
                    }
            );
            player.giveItemStack(letter);
        }

        // reset train
        try {
            resetTrain(world);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        gameComponent.setGameStatus(GameWorldComponent.GameStatus.ACTIVE);
        trainComponent.setTime(0);
        gameComponent.sync();
    }

    public static void finalizeGame(ServerWorld world) {
        TrainWorldComponent trainComponent = TMMComponents.TRAIN.get(world);
        trainComponent.setTrainSpeed(0);
        world.setTimeOfDay(6000);

        // reset train
        try {
            resetTrain(world);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        // discard all player bodies
        for (var body : world.getEntitiesByType(TMMEntities.PLAYER_BODY, playerBodyEntity -> true)) body.discard();

        // reset all players to adventure mode, clear inventory and teleport to spawn
        var teleportTarget = new TeleportTarget(world, new Vec3d(-872.5, 0, -323), Vec3d.ZERO, 90, 0, TeleportTarget.NO_OP);
        for (var player : world.getPlayers()) {
            player.changeGameMode(GameMode.ADVENTURE);
            // dismount all players as it can cause issues
            player.dismountVehicle();
            player.getInventory().clear();
            player.teleportTo(teleportTarget);
            PlayerMoodComponent.KEY.get(player).reset();
            PlayerStoreComponent.KEY.get(player).reset();
            PlayerPoisonComponent.KEY.get(player).reset();
        }

        // reset game component
        var gameComponent = TMMComponents.GAME.get(world);
        gameComponent.resetRoleLists();
        gameComponent.setGameStatus(GameWorldComponent.GameStatus.INACTIVE);
        trainComponent.setTime(0);
        gameComponent.sync();
    }

    public static boolean isPlayerEliminated(PlayerEntity player) {
        return player == null || !player.isAlive() || player.isCreative() || player.isSpectator();
    }

    public static void killPlayer(PlayerEntity player, boolean spawnBody) {
        if (player instanceof ServerPlayerEntity serverPlayerEntity) serverPlayerEntity.changeGameMode(GameMode.SPECTATOR);

        if (spawnBody) {
            PlayerBodyEntity body = TMMEntities.PLAYER_BODY.create(player.getWorld());
            body.setPlayerUuid(player.getUuid());

            Vec3d spawnPos = player.getPos().add(player.getRotationVector().normalize().multiply(1));

            body.refreshPositionAndAngles(spawnPos.getX(), player.getY(), spawnPos.getZ(), player.getHeadYaw(), 0f);
            body.setYaw(player.getHeadYaw());
            body.setHeadYaw(player.getHeadYaw());
            player.getWorld().spawnEntity(body);
        }
    }

    public static boolean isPlayerAliveAndSurvival(PlayerEntity player) {
        return player != null && !player.isSpectator() && !player.isCreative();
    }

    record BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
    }

    record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo) {
    }

    enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean allowsOverlap;

        Mode(final boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }

    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType(
            (maxCount, count) -> Text.stringifiedTranslatable("commands.clone.toobig", maxCount, count)
    );
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.failed"));
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = pos -> !pos.getBlockState().isAir();

    public static void resetTrain(ServerWorld serverWorld) throws CommandSyntaxException {
        BlockPos backupMinPos = BlockPos.ofFloored(TMMGameConstants.BACKUP_TRAIN_LOCATION.getMinPos());
        BlockPos backupMaxPos = BlockPos.ofFloored(TMMGameConstants.BACKUP_TRAIN_LOCATION.getMaxPos());
        BlockBox backupTrainBox = BlockBox.create(backupMinPos, backupMaxPos);
        BlockPos trainMinPos = BlockPos.ofFloored(TMMGameConstants.TRAIN_LOCATION.getMinPos());
        BlockPos trainMaxPos = trainMinPos.add(backupTrainBox.getDimensions());
        BlockBox trainBox = BlockBox.create(trainMinPos, trainMaxPos);

        Mode mode = Mode.FORCE;

        if (!mode.allowsOverlap() && trainBox.intersects(backupTrainBox)) {
            throw OVERLAP_EXCEPTION.create();
        } else {
            int i = backupTrainBox.getBlockCountX() * backupTrainBox.getBlockCountY() * backupTrainBox.getBlockCountZ();
            int j = serverWorld.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
            if (i > j) {
                throw TOO_BIG_EXCEPTION.create(j, i);
            } else if (serverWorld.isRegionLoaded(backupMinPos, backupMaxPos) && serverWorld.isRegionLoaded(trainMinPos, trainMaxPos)) {
                List<BlockInfo> list = Lists.newArrayList();
                List<BlockInfo> list2 = Lists.newArrayList();
                List<BlockInfo> list3 = Lists.newArrayList();
                Deque<BlockPos> deque = Lists.newLinkedList();
                BlockPos blockPos5 = new BlockPos(
                        trainBox.getMinX() - backupTrainBox.getMinX(), trainBox.getMinY() - backupTrainBox.getMinY(), trainBox.getMinZ() - backupTrainBox.getMinZ()
                );

                for (int k = backupTrainBox.getMinZ(); k <= backupTrainBox.getMaxZ(); k++) {
                    for (int l = backupTrainBox.getMinY(); l <= backupTrainBox.getMaxY(); l++) {
                        for (int m = backupTrainBox.getMinX(); m <= backupTrainBox.getMaxX(); m++) {
                            BlockPos blockPos6 = new BlockPos(m, l, k);
                            BlockPos blockPos7 = blockPos6.add(blockPos5);
                            CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(serverWorld, blockPos6, false);
                            BlockState blockState = cachedBlockPosition.getBlockState();

                            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos6);
                            if (blockEntity != null) {
                                BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                        blockEntity.createComponentlessNbt(serverWorld.getRegistryManager()), blockEntity.getComponents()
                                );
                                list2.add(new BlockInfo(blockPos7, blockState, blockEntityInfo));
                                deque.addLast(blockPos6);
                            } else if (!blockState.isOpaqueFullCube(serverWorld, blockPos6) && !blockState.isFullCube(serverWorld, blockPos6)) {
                                list3.add(new BlockInfo(blockPos7, blockState, null));
                                deque.addFirst(blockPos6);
                            } else {
                                list.add(new BlockInfo(blockPos7, blockState, null));
                                deque.addLast(blockPos6);
                            }
                        }
                    }
                }

                if (mode == Mode.MOVE) {
                    for (BlockPos blockPos8 : deque) {
                        BlockEntity blockEntity2 = serverWorld.getBlockEntity(blockPos8);
                        Clearable.clear(blockEntity2);
                        serverWorld.setBlockState(blockPos8, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }

                    for (BlockPos blockPos8 : deque) {
                        serverWorld.setBlockState(blockPos8, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }

                List<BlockInfo> list4 = Lists.newArrayList();
                list4.addAll(list);
                list4.addAll(list2);
                list4.addAll(list3);
                List<BlockInfo> list5 = Lists.reverse(list4);

                for (BlockInfo blockInfo : list5) {
                    BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos);
                    Clearable.clear(blockEntity3);
                    serverWorld.setBlockState(blockInfo.pos, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
                }

                int mx = 0;

                for (BlockInfo blockInfo2 : list4) {
                    if (serverWorld.setBlockState(blockInfo2.pos, blockInfo2.state, Block.NOTIFY_LISTENERS)) {
                        mx++;
                    }
                }

                for (BlockInfo blockInfo2x : list2) {
                    BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo2x.pos);
                    if (blockInfo2x.blockEntityInfo != null && blockEntity4 != null) {
                        blockEntity4.readComponentlessNbt(blockInfo2x.blockEntityInfo.nbt, serverWorld.getRegistryManager());
                        blockEntity4.setComponents(blockInfo2x.blockEntityInfo.components);
                        blockEntity4.markDirty();
                    }

                    serverWorld.setBlockState(blockInfo2x.pos, blockInfo2x.state, Block.NOTIFY_LISTENERS);
                }

                for (BlockInfo blockInfo2x : list5) {
                    serverWorld.updateNeighbors(blockInfo2x.pos, blockInfo2x.state.getBlock());
                }

                serverWorld.getBlockTickScheduler().scheduleTicks(serverWorld.getBlockTickScheduler(), backupTrainBox, blockPos5);
                if (mx == 0) {
                    throw FAILED_EXCEPTION.create();
                } else {
//                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
//                        player.sendMessage(Text.translatable("commands.clone.success", mx));
//                    }
                }
            } else {
                throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
            }
        }

        // discard all player bodies and items
        for (PlayerBodyEntity body : serverWorld.getEntitiesByType(TMMEntities.PLAYER_BODY, playerBodyEntity -> true)) {
            body.discard();
        }
        for (ItemEntity item : serverWorld.getEntitiesByType(EntityType.ITEM, playerBodyEntity -> true)) {
            item.discard();
        }

    }

    public enum WinStatus {
        NONE, HITMEN, PASSENGERS
    }
}
