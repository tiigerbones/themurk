package com.enchantedwisp.murk.client.sound;

import com.enchantedwisp.murk.registry.Sounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PhaseSoundInstance extends AbstractTickableSoundInstance {

    private static final int FADE_IN_TICKS = 40;   // 2 seconds
    private static final int FADE_OUT_TICKS = 20;  // 1 second
    private static final float MAX_VOLUME = 1.0F;

    private final Player player;

    private int fadeTimer = 0;
    private boolean fadingOut = false;

    public PhaseSoundInstance(Player player) {
        super(Sounds.MURK_WHISPERS,
                SoundSource.PLAYERS,
                SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
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
        stop();
    }

    public boolean isFadingOut() {
        return fadingOut;
    }

    @Override
    public void tick() {

        if (!player.isAlive()) {
            beginFadeOut();
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        if (fadingOut) {

            fadeTimer++;

            float progress = fadeTimer / (float) FADE_OUT_TICKS;
            this.volume = MAX_VOLUME * (float) Math.cos(progress * Math.PI * 0.5);

            if (this.volume < 0F) {
                this.volume = 0F;
            }

            if (fadeTimer >= FADE_OUT_TICKS) {
                this.stop();
            }

            return;
        }

        if (fadeTimer < FADE_IN_TICKS) {
            fadeTimer++;
            float progress = fadeTimer / (float) FADE_IN_TICKS;
            this.volume = MAX_VOLUME * (float) Math.sin(progress * Math.PI * 0.5);
        } else {
            this.volume = MAX_VOLUME;
        }
    }
}