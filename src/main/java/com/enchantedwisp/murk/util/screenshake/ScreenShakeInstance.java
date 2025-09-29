package com.enchantedwisp.murk.util.screenshake;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class ScreenShakeInstance {
    private final float intensity;
    private final int maxDuration; // store original duration
    private int ticksRemaining;
    private final Random random = new Random();

    public ScreenShakeInstance(float intensity, int durationTicks) {
        this.intensity = intensity;
        this.ticksRemaining = durationTicks;
        this.maxDuration = durationTicks;
    }

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public boolean isFinished() {
        return ticksRemaining <= 0;
    }

    public float[] getCurrentOffsets() {
        if (!isFinished()) {
            // fade based on percentage of time left
            float fade = (float) ticksRemaining / (float) maxDuration;

            float yawOffset   = (random.nextFloat() - 0.5f) * intensity * fade * 2.0f;
            float pitchOffset = (random.nextFloat() - 0.5f) * intensity * fade * 2.0f;

            return new float[] {yawOffset, pitchOffset};
        }
        return new float[] {0.0f, 0.0f};
    }
}

