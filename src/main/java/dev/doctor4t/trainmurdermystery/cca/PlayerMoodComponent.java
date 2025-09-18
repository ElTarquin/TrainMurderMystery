package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.TMMGameConstants;
import dev.doctor4t.trainmurdermystery.util.Carriage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Objects;

public class PlayerMoodComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final Identifier MOOD = TMM.id("mood");
    public static final ComponentKey<PlayerMoodComponent> KEY = ComponentRegistry.getOrCreate(MOOD, PlayerMoodComponent.class);
    private final PlayerEntity player;
    private TrainPreference currentPreference = TrainPreference.TRUE;
    private int nextPreferenceTimer = 0;
    public float mood = 1f;
    private boolean fulfilled = false;
    private String previousPreferenceText = "";
    private String preferenceText = "";
    private float preferenceTextAlpha = 0f;

    public PlayerMoodComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Environment(EnvType.CLIENT)
    public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!Objects.equals(this.previousPreferenceText, this.preferenceText)) {
            this.preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, this.preferenceTextAlpha, 0f);
            if (this.preferenceTextAlpha <= 0.01f) this.previousPreferenceText = this.preferenceText;
        } else {
            this.preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, this.preferenceTextAlpha, 1f);
        }
        if (this.previousPreferenceText.isEmpty()) return;
        var renderer = MinecraftClient.getInstance().textRenderer;
        var textWidth = renderer.getWidth(this.previousPreferenceText);
        context.getMatrices().push();
        context.getMatrices().translate(- (10 + textWidth) * (1f - this.preferenceTextAlpha), 0, 0);
        context.drawTextWithShadow(renderer, this.previousPreferenceText, 8, 8, MathHelper.packRgb(1f, 1f, 1f) | ((int) (this.preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(12, 10 + renderer.fontHeight, 0);
        context.getMatrices().scale((textWidth - 8) * this.mood, 1, 1);
        context.fill(0, 0, 1, 1, MathHelper.hsvToRgb(this.mood / 3.0F, 1.0F, 1.0F) | ((int) (this.preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
    }

    public void reset() {
        this.currentPreference = TrainPreference.TRUE;
        this.nextPreferenceTimer = 1;
        this.fulfilled = false;
        this.setMood(1f);
        this.sync();
    }

    @Override
    public void clientTick() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isRunning()) return;
        this.tickMood();
    }

    @Override
    public void serverTick() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isRunning()) return;
        var shouldSync = false;
        if (this.nextPreferenceTimer > 0) {
            this.nextPreferenceTimer--;
            if (this.nextPreferenceTimer <= 0) {
                this.generatePreference();
                this.preferenceText = this.currentPreference.getString();
                this.nextPreferenceTimer = (int) (this.player.getRandom().nextFloat() * (TMMGameConstants.MAX_PREFERENCE_COOLDOWN - TMMGameConstants.MIN_PREFERENCE_COOLDOWN) + TMMGameConstants.MIN_PREFERENCE_COOLDOWN);
                shouldSync = true;
            }
        }
        if (this.currentPreference.isFulfilled(this.player)) {
            if (!this.fulfilled) shouldSync = true;
            this.fulfilled = true;
        } else {
            if (this.fulfilled) shouldSync = true;
            this.fulfilled = false;
        }
        this.tickMood();
        if (shouldSync) this.sync();
    }

    public void tickMood() {
        if (this.fulfilled) {
            this.setMood(this.mood + TMMGameConstants.MOOD_GAIN);
        } else {
            this.setMood(this.mood - TMMGameConstants.MOOD_DRAIN);
        }
    }

    public void setMood(float mood) {
        this.mood = Math.clamp(mood, 0, 1);
    }

    private void generatePreference() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isCivilian(this.player)) {
            this.currentPreference = TrainPreference.TRUE;
            return;
        }

        var random = this.player.getRandom();

        // 5% chance to be happy with anything
        if (random.nextFloat() < 0.05f) {
            this.currentPreference = TrainPreference.TRUE;
            return;
        }

        // 30% chance to want to be outside
        if (random.nextFloat() < 0.3f) {
            this.currentPreference = TrainPreference.OUTSIDE;
            return;
        }

        // 10% chance to be tired
        if (random.nextFloat() < 0.1f) {
            this.currentPreference = TrainPreference.SLEEP;
            return;
        }

        var carriages = TMMGameConstants.CARRIAGES;
        if (carriages.isEmpty()) {
            this.currentPreference = TrainPreference.TRUE;
        } else {
            this.currentPreference = new CarriageTrainPreference(carriages.get(this.player.getRandom().nextInt(carriages.size())));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putFloat("Mood", this.mood);
        tag.putBoolean("Fulfilled", this.fulfilled);
        tag.putString("Preference", this.preferenceText);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.mood = tag.contains("Mood") ? tag.getFloat("Mood") : 1f;
        this.fulfilled = tag.contains("Fulfilled") && tag.getBoolean("Fulfilled");
        this.preferenceText = tag.contains("Preference") ? tag.getString("Preference") : "";
    }

    public interface TrainPreference {
        TrainPreference TRUE = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return true;
            }

            @Override
            public @NotNull String getString() {
                return "";
            }
        };
        TrainPreference OUTSIDE = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return player.getWorld().isSkyVisible(player.getBlockPos());
            }

            @Override
            public @NotNull String getString() {
                return "You should get some fresh air";
            }
        };
        TrainPreference SLEEP = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return player.isSleeping();
            }

            @Override
            public @NotNull String getString() {
                return "You should get some sleep";
            }
        };

        boolean isFulfilled(PlayerEntity player);

        String getString();
    }

    public record CarriageTrainPreference(Carriage carriage) implements TrainPreference {
        @Override
        public boolean isFulfilled(@NotNull PlayerEntity player) {
            return this.carriage.areas().stream().anyMatch(box -> box.contains(player.getPos()));
        }

        @Override
        public @NotNull String getString() {
            return "You feel like visiting %s %s carriage".formatted(this.carriage.areas().size() == 1 ? "the" : "a", this.carriage.name());
        }
    }
}