package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.sound.PhaseSoundManager;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TheMurkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client");
    private static final Map<UUID, Boolean> hadMurkGraspLastTick = new HashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing The Murk client");


        // Register client tick event for tick-based logic only (sounds, vanish sound, effect tracking)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ScreenEffectManager.tick();
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                UUID playerId = player.getUUID();
                boolean hasMurkGrasp = player.hasEffect(Effects.MURKS_GRASP);

                // Manage sound effects
                if (hasMurkGrasp) {
                    PhaseSoundManager.startSound(player);
                } else {
                    PhaseSoundManager.stopSound(player);
                }

                // Play murk_vanish sound when MurksGraspEffect expires
                boolean hadEffectLastTick = hadMurkGraspLastTick.getOrDefault(playerId, false);
                if (hadEffectLastTick && !hasMurkGrasp) {
                    Minecraft.getInstance().getSoundManager().play(
                            new SimpleSoundInstance(
                                    Sounds.MURK_VANISH,
                                    SoundSource.PLAYERS,
                                    1.0f, // Volume
                                    1.0f, // Pitch
                                    SoundInstance.createUnseededRandom(),
                                    player.getX(),
                                    player.getY(),
                                    player.getZ()
                            )
                    );
                }

                // Update effect tracking
                hadMurkGraspLastTick.put(playerId, hasMurkGrasp);
            }
        });
    }
}