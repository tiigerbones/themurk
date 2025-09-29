package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.util.screenshake.ScreenShakeInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ScreenEffectManager {
    private static ScreenShakeInstance activeShake = null;

    public static void applyScreenShake(float intensity, int durationTicks) {
        intensity = Math.max(0.0f, Math.min(2.0f, intensity));
        durationTicks = Math.max(1, Math.min(100, durationTicks));
        activeShake = new ScreenShakeInstance(intensity, durationTicks);
    }

    public static void tick() {
        if (activeShake != null) {
            activeShake.tick();
            if (activeShake.isFinished()) {
                activeShake = null;
            }
        }
    }

    public static float[] getCameraShakeOffsets() {
        return activeShake != null ? activeShake.getCurrentOffsets() : new float[] {0.0f, 0.0f};
    }
}
