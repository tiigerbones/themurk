package com.enchantedwisp.murk.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MurkSoundManager {
    private static final Map<UUID, MurkGraspSoundInstance> activeSounds = new HashMap<>();

    public static void startSound(PlayerEntity player) {
        if (player == null || !player.getWorld().isClient) return;

        UUID playerId = player.getUuid();
        // Only start a new sound if none exists for this player
        if (!activeSounds.containsKey(playerId)) {
            MurkGraspSoundInstance sound = new MurkGraspSoundInstance(player);
            activeSounds.put(playerId, sound);
            MinecraftClient.getInstance().getSoundManager().play(sound);
        }
    }

    public static void stopSound(PlayerEntity player) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        MurkGraspSoundInstance sound = activeSounds.remove(playerId);
        if (sound != null) {
            MinecraftClient.getInstance().getSoundManager().stop(sound);
        }
    }

    public static void stopAllSounds() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        for (MurkGraspSoundInstance sound : activeSounds.values()) {
            soundManager.stop(sound);
        }
        activeSounds.clear();
    }
}