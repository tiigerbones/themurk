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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.shedaniel.autoconfig.AutoConfig;

public class LightLevelTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_tracker");
    private static final int TICKS_UNTIL_WARNING = 100; // 5 seconds
    private static final int TICKS_AFTER_WARNING = 200; // 10 seconds
    private static final Map<UUID, Integer> playerLowLightTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerWarned = new HashMap<>();
    private static final Map<UUID, Boolean> playerDurationReduced = new HashMap<>();
    private static final Map<UUID, Integer> playerEffectTicks = new HashMap<>();

    public static void register() {
        LOGGER.info("Registering LightLevelTracker");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MurkConfig config;
            try {
                config = AutoConfig.getConfigHolder(MurkConfig.class).getConfig();
            } catch (Exception e) {
                LOGGER.error("Failed to load MurkConfig, using default values", e);
                config = new MurkConfig(); // Fallback to default config
            }
            int lightThreshold = config.lightThreshold;
            int litAreaDurationTicks = (int) (config.litAreaEffectDuration * 20); // Convert seconds to ticks

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getBlockPos();

                // Skip Creative mode players unless enabled
                if (player.isCreative() && !config.enableCreativeEffect) {
                    resetPlayer(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (config.blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                        LOGGER.debug("Removed MurksGraspEffect for Creative player {}", player.getName().getString());
                    }
                    continue;
                }

                // Check if player is in an allowed dimension
                Identifier dimensionId = world.getRegistryKey().getValue();
                if (!config.dimensions.contains(dimensionId.toString())) {
                    resetPlayer(playerId);
                    continue;
                }

                // Skip light checks if underwater and not enabled
                if (!config.enableUnderwaterLightCheck && player.isSubmergedInWater()) {
                    resetPlayer(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (config.blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                        LOGGER.debug("Removed MurksGraspEffect for player {} underwater", player.getName().getString());
                    }
                    continue;
                }

                // Check total light level (block + item)
                int lightLevel = getEffectiveLightLevel(player, config);
                LOGGER.debug("Player {} at {} in dimension {} has light level {}",
                        player.getName().getString(), pos, dimensionId, lightLevel);

                if (lightLevel < lightThreshold) {
                    // Player is in low light
                    if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        // Increment tick counter if effect not yet applied
                        int ticks = playerLowLightTicks.getOrDefault(playerId, 0) + 1;
                        playerLowLightTicks.put(playerId, ticks);

                        // Send warning message after 5 seconds if enabled
                        if (config.enableWarningText && ticks >= TICKS_UNTIL_WARNING && !playerWarned.getOrDefault(playerId, false)) {
                            player.sendMessage(
                                    Text.literal("An evil presence lurks in the dark nearby...").styled(style -> style.withColor(0xFF5555)),
                                    false
                            );
                            playerWarned.put(playerId, true);
                            LOGGER.debug("Sent warning message to player {}", player.getName().getString());
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
                            if (config.blindnessEnabled) {
                                player.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.BLINDNESS,
                                        -1, // Infinite duration
                                        0, // Amplifier 0
                                        false, // Not ambient
                                        false // Hide particles
                                ));
                            }
                            LOGGER.info("Applied MurksGraspEffect (infinite) to player {}", player.getName().getString());
                            resetPlayer(playerId); // Reset timer after applying effect
                            playerEffectTicks.put(playerId, 0); // Initialize effect timer
                        }
                    } else {
                        // Increment effect duration
                        playerEffectTicks.put(playerId, playerEffectTicks.getOrDefault(playerId, 0) + 1);
                    }
                    // Reset duration reduction flag when entering low light
                    playerDurationReduced.remove(playerId);
                } else {
                    // Player is in lit area (light level >= threshold)
                    if (player.hasStatusEffect(Effects.MURKS_GRASP) && !playerDurationReduced.getOrDefault(playerId, false)) {
                        // Remove and reapply MurksGraspEffect with configured duration
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        player.addStatusEffect(new StatusEffectInstance(
                                Effects.MURKS_GRASP,
                                litAreaDurationTicks,
                                0, // Amplifier 0
                                false, // Not ambient
                                true // Show particles
                        ));
                        // Remove and reapply Blindness effect with configured duration if enabled
                        if (config.blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                            player.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.BLINDNESS,
                                    litAreaDurationTicks,
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    false // Hide particles
                            ));
                        }
                        playerDurationReduced.put(playerId, true);
                        playerEffectTicks.put(playerId, 0); // Reset effect ticks when entering lit area
                        LOGGER.debug("Reduced MurksGraspEffect and {} duration to {} ticks for player {}",
                                config.blindnessEnabled ? "Blindness" : "no Blindness", litAreaDurationTicks, player.getName().getString());
                    }
                    // Reset timer and warning state
                    resetPlayer(playerId);
                }

                // Clear duration reduction flag and effect ticks if effect expires
                if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    playerDurationReduced.remove(playerId);
                    playerEffectTicks.remove(playerId);
                }
            }
        });
    }

    private static int getEffectiveLightLevel(ServerPlayerEntity player, MurkConfig config) {
        int blockLight = player.getWorld().getLightLevel(player.getBlockPos());
        int itemLight = config.enableDynamicLighting && DynamicLightingHandler.isDynamicLightingModLoaded()
                ? DynamicLightingHandler.getPlayerLightLevel(player)
                : 0;
        int totalLight = Math.max(blockLight, itemLight);

        LOGGER.debug("Player {}: Block light {}, Item light {}, Total light {}",
                player.getName().getString(), blockLight, itemLight, totalLight);
        return totalLight;
    }

    private static void resetPlayer(UUID playerId) {
        playerLowLightTicks.remove(playerId);
        playerWarned.remove(playerId);
        playerEffectTicks.remove(playerId);
        LOGGER.debug("Reset light tracking for player UUID {}", playerId);
    }
}