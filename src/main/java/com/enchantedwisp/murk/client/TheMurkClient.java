package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.shader.ChromaticAberrationPostProcessor;
import com.enchantedwisp.murk.client.sound.MurkSoundManager;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TheMurkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client");
    private static final Map<UUID, Boolean> hadMurkGraspLastTick = new HashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing The Murk client");

        // Register post-processors
        /* LOG FOR DEV
        LOGGER.info("Registering ChromaticAberrationPostProcessor");
         */
        PostProcessHandler.addInstance(ChromaticAberrationPostProcessor.INSTANCE);

        // Register client tick event to toggle effects, sounds, and play murk_vanish when effect expires
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                UUID playerId = player.getUuid();
                boolean hasMurkGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);

                // Toggle ChromaticAberrationPostProcessor
                if (hasMurkGrasp && !ChromaticAberrationPostProcessor.INSTANCE.isActive()) {
                    ChromaticAberrationPostProcessor.INSTANCE.setActive(true);
                    /* LOG FOR DEV
                    LOGGER.debug("Enabled ChromaticAberrationPostProcessor for player {}", player.getName().getString());
                     */
                } else if (!hasMurkGrasp && ChromaticAberrationPostProcessor.INSTANCE.isActive()) {
                    ChromaticAberrationPostProcessor.INSTANCE.setActive(false);
                    /* LOG FOR DEV
                    LOGGER.debug("Disabled ChromaticAberrationPostProcessor for player {}", player.getName().getString());
                     */
                }

                // Manage sound effects
                if (hasMurkGrasp) {
                    MurkSoundManager.startSound(player);
                } else {
                    MurkSoundManager.stopSound(player);
                }

                // Play murk_vanish sound when MurksGraspEffect expires
                boolean hadEffectLastTick = hadMurkGraspLastTick.getOrDefault(playerId, false);
                if (hadEffectLastTick && !hasMurkGrasp) {
                    MinecraftClient.getInstance().getSoundManager().play(
                            new PositionedSoundInstance(
                                    Sounds.MURK_VANISH,
                                    SoundCategory.PLAYERS,
                                    1.0f, // Volume
                                    1.0f, // Pitch
                                    SoundInstance.createRandom(),
                                    player.getX(),
                                    player.getY(),
                                    player.getZ()
                            )
                    );
                    /* LOG FOR DEV
                    LOGGER.debug("Played murk_vanish sound for player {} as MurksGraspEffect expired", player.getName().getString());
                     */
                }

                // Update effect tracking
                hadMurkGraspLastTick.put(playerId, hasMurkGrasp);
            }
        });
    }
}