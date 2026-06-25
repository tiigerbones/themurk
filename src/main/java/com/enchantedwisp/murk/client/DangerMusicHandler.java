package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.network.DangerMusicNetworking;
import com.enchantedwisp.murk.util.ConfigCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.MusicTracker;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public final class DangerMusicHandler {

    private static boolean musicSuppressed = false;
    private static boolean wasSuppressedLastTick = false;

    /** Reflection field to access the internal music cooldown timer */
    private static Field timeUntilNextSongField;

    static {
        try {
            timeUntilNextSongField = MusicTracker.class.getDeclaredField("timeUntilNextSong");
            timeUntilNextSongField.setAccessible(true);
        } catch (Exception e) {
            TheMurk.LOGGER.warn("Could not access MusicTracker.timeUntilNextSong via reflection.");
        }
    }

    private DangerMusicHandler() {}

    public static void setSuppressed(boolean suppress) {
        if (!ConfigCache.isMusicSuppressionEnabled()) {
            musicSuppressed = false;
            return;
        }
        if (musicSuppressed != suppress) {
            musicSuppressed = suppress;
            TheMurk.LOGGER.debug("Music suppression state changed to: {}", suppress);
        }
    }

    public static boolean isSuppressed() {
        return musicSuppressed && ConfigCache.isMusicSuppressionEnabled();
    }

    /**
     * Uses reflection to reset Minecraft's internal music timer so it tries
     * to play a track on the next tick instead of waiting for the normal delay.
     */
    private static void forceMusicToPlaySoon(MusicTracker tracker) {
        if (timeUntilNextSongField == null || tracker == null) return;
        try {
            timeUntilNextSongField.setInt(tracker, 0);
        } catch (Exception ignored) {}
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                DangerMusicNetworking.DANGER_MUSIC_SUPPRESS,
                (client, handler, buf, responseSender) -> {
                    boolean suppress = buf.readBoolean();
                    client.execute(() -> setSuppressed(suppress));
                }
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean enabled = ConfigCache.isMusicSuppressionEnabled();

            if (!enabled) {
                // Feature disabled → make sure we're not suppressing
                if (musicSuppressed) {
                    musicSuppressed = false;
                    wasSuppressedLastTick = false;
                }
                return;
            }

            if (musicSuppressed) {
                MusicTracker musicTracker = client.getMusicTracker();
                if (musicTracker != null) {
                    musicTracker.stop();
                }
                wasSuppressedLastTick = true;
            } else {
                if (wasSuppressedLastTick) {
                    wasSuppressedLastTick = false;

                    if (Math.random() < 0.10) {
                        TheMurk.LOGGER.debug("Music suppression ended - forcing faster resume (10% chance)");
                        MusicTracker musicTracker = client.getMusicTracker();
                        forceMusicToPlaySoon(musicTracker);
                    }
                }
            }
        });

        TheMurk.LOGGER.info("DangerMusicHandler registered (toggleable music suppression)");
    }
}