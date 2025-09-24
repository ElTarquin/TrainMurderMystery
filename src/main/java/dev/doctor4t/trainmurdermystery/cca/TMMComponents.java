package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class TMMComponents implements WorldComponentInitializer, EntityComponentInitializer, ScoreboardComponentInitializer {
    public static final ComponentKey<TrainWorldComponent> TRAIN = ComponentRegistry.getOrCreate(TMM.id("train"), TrainWorldComponent.class);
    public static final ComponentKey<GameWorldComponent> GAME = ComponentRegistry.getOrCreate(TMM.id("game"), GameWorldComponent.class);

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(TRAIN, TrainWorldComponent::new);
        registry.register(GAME, GameWorldComponent::new);
        registry.register(WorldBlackoutComponent.KEY, WorldBlackoutComponent::new);
    }

    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, PlayerMoodComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerMoodComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerStoreComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerStoreComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerPoisonComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerPoisonComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerPsychoComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerPsychoComponent::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(ScoreboardRoleSelectorComponent.KEY, (scoreboard, server) -> new ScoreboardRoleSelectorComponent(scoreboard, server));
    }
}