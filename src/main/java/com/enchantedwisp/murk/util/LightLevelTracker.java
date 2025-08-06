package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.registry.Effects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightLevelTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_tracker");
    private static final int TICKS_UNTIL_WARNING = 100; // 5 seconds
    private static final int TICKS_AFTER_WARNING = 200; // 10 seconds
    private static final int LIT_AREA_DURATION = 80; // 4 seconds
    private static final Map<UUID, Integer> playerLowLightTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerWarned = new HashMap<>();
    private static final Map<UUID, Boolean> playerDurationReduced = new HashMap<>();

    public static void register() {
        LOGGER.info("Registering LightLevelTracker");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MurkConfig config = MurkConfig.getInstance();
            int lightThreshold = config.lightThreshold;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getBlockPos();

                // Check total light level (block + sky) at player's position
                int lightLevel = world.getLightLevel(pos);
                LOGGER.debug("Player {} at {} has light level {}", player.getName().getString(), pos, lightLevel);

                if (lightLevel < lightThreshold) {
                    // Player is in low light
                    if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        // Increment tick counter if effect not yet applied
                        int ticks = playerLowLightTicks.getOrDefault(playerId, 0) + 1;
                        playerLowLightTicks.put(playerId, ticks);

                        // Send warning title after 5 seconds if enabled
                        if (config.enableWarningText && ticks >= TICKS_UNTIL_WARNING && !playerWarned.getOrDefault(playerId, false)) {
                            player.sendMessage(Text.literal("Find a lit area soon!").styled(style -> style.withColor(0xFF5555)), false);
                            playerWarned.put(playerId, true);
                            LOGGER.debug("Sent warning title to player {}", player.getName().getString());
                        }

                        // Apply effect with infinite duration after 15 seconds (5 + 10)
                        if (ticks >= TICKS_UNTIL_WARNING + TICKS_AFTER_WARNING) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    Effects.MURKS_GRASP,
                                    -1, // Infinite duration
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    true // Show particles
                            ));
                            LOGGER.info("Applied MurksGraspEffect (infinite) to player {}", player.getName().getString());
                            resetPlayer(playerId); // Reset timer after applying effect
                        }
                    }
                    // Reset duration reduction flag when entering low light
                    playerDurationReduced.remove(playerId);
                } else {
                    // Player is in lit area (light level >= threshold)
                    if (player.hasStatusEffect(Effects.MURKS_GRASP) && !playerDurationReduced.getOrDefault(playerId, false)) {
                        // Remove and reapply MurksGraspEffect with 4-second duration
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        player.addStatusEffect(new StatusEffectInstance(
                                Effects.MURKS_GRASP,
                                LIT_AREA_DURATION, // 4 seconds
                                0, // Amplifier 0
                                false, // Not ambient
                                true // Show particles
                        ));
                        // Remove and reapply Blindness effect with 4-second duration
                        player.removeStatusEffect(StatusEffects.BLINDNESS);
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.BLINDNESS,
                                LIT_AREA_DURATION, // 4 seconds
                                0, // Amplifier 0
                                false, // Not ambient
                                false // Hide particles
                        ));
                        playerDurationReduced.put(playerId, true);
                        LOGGER.debug("Reduced MurksGraspEffect and Blindness duration to 4 seconds for player {}", player.getName().getString());
                    }
                    // Reset timer and warning state
                    resetPlayer(playerId);
                }

                // Clear duration reduction flag if effect expires
                if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    playerDurationReduced.remove(playerId);
                }
            }
        });
    }

    private static void resetPlayer(UUID playerId) {
        playerLowLightTicks.remove(playerId);
        playerWarned.remove(playerId);
        LOGGER.debug("Reset light tracking for player UUID {}", playerId);
    }
}