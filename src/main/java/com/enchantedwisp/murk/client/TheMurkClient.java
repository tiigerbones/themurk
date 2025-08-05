package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.shader.TestPostProcessor;
import com.enchantedwisp.murk.client.sound.MurkSoundManager;
import com.enchantedwisp.murk.client.shader.ChromaticAberrationPostProcessor;
import com.enchantedwisp.murk.registry.Effects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;


public class TheMurkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing The Murk client");

        // Register post-processors
        LOGGER.info("Registering ChromaticAberrationPostProcessor");
        PostProcessHandler.addInstance(ChromaticAberrationPostProcessor.INSTANCE);
        LOGGER.info("Registering TestPostProcessor");
        PostProcessHandler.addInstance(TestPostProcessor.INSTANCE);


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