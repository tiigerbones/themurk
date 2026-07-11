package com.enchantedwisp.murk.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PhaseSoundManager {

    private static final Map<UUID, PhaseSoundInstance> activeSounds = new HashMap<>();

    public static void startSound(PlayerEntity player) {
        if (player == null || !player.getEntityWorld().isClient()) return;

        UUID id = player.getUuid();

        PhaseSoundInstance existing = activeSounds.get(id);

        if (existing == null || existing.isDone()) {
            if (existing != null) activeSounds.remove(id);

            PhaseSoundInstance sound = new PhaseSoundInstance(player);
            activeSounds.put(id, sound);
            MinecraftClient.getInstance().getSoundManager().play(sound);
        }
    }

    public static void stopSound(PlayerEntity player) {
        if (player == null) return;

        PhaseSoundInstance sound = activeSounds.get(player.getUuid());
        if (sound != null) {
            sound.beginFadeOut();
        }
    }

    public static void stopAllSounds() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        for (PhaseSoundInstance sound : activeSounds.values()) {
            sound.stopImmediately();
        }
        activeSounds.clear();
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, PhaseSoundInstance>> it = activeSounds.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isDone()) {
                it.remove();
            }
        }
    }
}