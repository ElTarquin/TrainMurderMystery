package dev.doctor4t.trainmurdermystery.util;

public class PoisonUtils {
    private static float pulseProgress = 0f;
    private static int poisonTicks = 0;
    private static boolean pulsing = false;

    public static void startPulse(int poisonedTicks) {
        pulseProgress = 0f;
        pulsing = true;
        poisonTicks = poisonedTicks;
    }

    public static float getFovMultiplier(float tickDelta) {
        if (!pulsing) return 1f;

        pulseProgress += tickDelta * 0.1f;

        if (pulseProgress >= 1f) {
            pulsing = false;
            pulseProgress = 0f;
            return 1f;
        }

        float maxAmplitude = 0.1f;
        float minAmplitude = 0.025f;

        float amplitude = minAmplitude + (maxAmplitude - minAmplitude) * (1f - ((float) poisonTicks / 1200f));

        float result;

        if (pulseProgress < 0.25f) {
            result = 1f - amplitude * (float) Math.sin(Math.PI * (pulseProgress / 0.25f));
        } else if (pulseProgress < 0.5f) {
            result = 1f - amplitude * (float) Math.sin(Math.PI * ((pulseProgress - 0.25f) / 0.25f));
        } else { // rest
            result = 1f;
        }

        return result;
    }
}
