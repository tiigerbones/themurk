package com.enchantedwisp.murk.events;

import com.enchantedwisp.murk.registry.Sounds;
import com.enchantedwisp.murk.util.ConfigCache;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;

public class NightEvents {

    private static final float VOLUME = 0.90f;

    private static SoundInstance currentMusic;
    private static boolean wasNight = false;
    private static boolean playTonight = false;

    private NightEvents() {}

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.world == null || client.player == null) {
                return;
            }

            if (!ConfigCache.isMurkyNightMusicEnabled()) {
                return;
            }

            long time = client.world.getTimeOfDay() % 24000;
            boolean isNight = time >= 13000 && time < 23000;

            if (isNight && !wasNight) {
                wasNight = true;

                // 35% chance to play murky night music this night
                playTonight = client.world.random.nextFloat() < 0.35f;

                if (playTonight) {
                    // Only stop normal music if we're actually playing our track
                    client.getMusicTracker().stop();

                    currentMusic = PositionedSoundInstance.master(
                            Sounds.MURK_NIGHT_AMBIENCE1,
                            VOLUME
                    );

                    client.getSoundManager().play(currentMusic);
                }
            }
            // Keep Audio Looping
            if (isNight && playTonight &&
                    currentMusic != null &&
                    !client.getSoundManager().isPlaying(currentMusic)) {

                currentMusic = PositionedSoundInstance.master(
                        Sounds.MURK_NIGHT_AMBIENCE1,
                        VOLUME
                );

                client.getSoundManager().play(currentMusic);
            }

            if (!isNight && wasNight) {
                wasNight = false;
                playTonight = false;

                if (currentMusic != null) {
                    client.getSoundManager().stop(currentMusic);
                    currentMusic = null;
                }
            }
        });
    }
}