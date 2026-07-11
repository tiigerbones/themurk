package com.enchantedwisp.murk.client.sound;

import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class PhaseSoundInstance extends MovingSoundInstance {

    private static final int FADE_IN_TICKS = 40;   // 2 seconds
    private static final int FADE_OUT_TICKS = 20;  // 1 second
    private static final float MAX_VOLUME = 1.0F;

    private final PlayerEntity player;
    private int fadeTimer = 0;
    private boolean fadingOut = false;

    public PhaseSoundInstance(PlayerEntity player) {
        super(Sounds.MURK_WHISPERS, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    public void beginFadeOut() {
        if (!fadingOut) {
            fadingOut = true;
            fadeTimer = 0;
        }
    }

    public void stopImmediately() {
        this.setDone();
    }

    public boolean isFadingOut() {
        return fadingOut;
    }

    @Override
    public void tick() {
        // Trigger graceful fade-out when effect ends or player dies
        if (!this.player.isAlive() || !this.player.hasStatusEffect(Effects.MURKS_GRASP)) {
            beginFadeOut();
        }

        // Always follow the player
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        if (fadingOut) {
            fadeTimer++;
            float progress = fadeTimer / (float) FADE_OUT_TICKS;
            this.volume = MAX_VOLUME * (float) Math.cos(progress * Math.PI * 0.5);
            if (this.volume < 0F) this.volume = 0F;

            if (fadeTimer >= FADE_OUT_TICKS) {
                this.setDone();
            }
            return;
        }

        // Fade-in on start
        if (fadeTimer < FADE_IN_TICKS) {
            fadeTimer++;
            float progress = fadeTimer / (float) FADE_IN_TICKS;
            this.volume = MAX_VOLUME * (float) Math.sin(progress * Math.PI * 0.5);
        } else {
            this.volume = MAX_VOLUME;
        }
    }
}