package com.enchantedwisp.murk.client.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.player.Player;

public class PhaseSoundManager {
    private static final Map<UUID, PhaseSoundInstance> activeSounds = new HashMap<>();

    public static void startSound(Player player) {
        if (player == null || !player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        // Only start a new sound if none exists for this player
        if (!activeSounds.containsKey(playerId)) {
            PhaseSoundInstance sound = new PhaseSoundInstance(player);
            activeSounds.put(playerId, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
    }

    public static void stopSound(Player player) {
        if (player == null) return;

        UUID playerId = player.getUUID();
        PhaseSoundInstance sound = activeSounds.remove(playerId);
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }

    public static void stopAllSounds() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        for (PhaseSoundInstance sound : activeSounds.values()) {
            soundManager.stop(sound);
        }
        activeSounds.clear();
    }
}