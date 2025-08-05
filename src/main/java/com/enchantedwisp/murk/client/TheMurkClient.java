package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.registry.Effects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class TheMurkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    MurkSoundManager.startSound(player);
                } else {
                    MurkSoundManager.stopSound(player);
                }
            }
        });
    }
}