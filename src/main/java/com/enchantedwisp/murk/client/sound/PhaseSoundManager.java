package com.enchantedwisp.murk.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PhaseSoundManager {
    private static final Map<UUID, PhaseSoundInstance> activeSounds = new HashMap<>();

    public static void startSound(Player player) {
        if (player == null || !player.level().isClientSide) {
            return;
        }

        UUID id = player.getUUID();

        PhaseSoundInstance existing = activeSounds.get(id);

        if (existing == null || existing.isStopped()) {
            PhaseSoundInstance sound = new PhaseSoundInstance(player);
            activeSounds.put(id, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
    }

    public static void stopSound(Player player) {
        if (player == null) return;

        PhaseSoundInstance sound = activeSounds.get(player.getUUID());

        if (sound != null) {
            sound.beginFadeOut();
        }
    }

    public static void tick() {

        Iterator<Map.Entry<UUID, PhaseSoundInstance>> iterator = activeSounds.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PhaseSoundInstance> entry = iterator.next();

            if (entry.getValue().isStopped()) {
                iterator.remove();
            }
        }
    }

    public static void stopAllSounds() {
        for (PhaseSoundInstance sound : activeSounds.values()) {
            sound.stopImmediately();
        }
        activeSounds.clear();
    }
}