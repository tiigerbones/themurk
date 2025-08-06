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

public class LightLevelTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_tracker");
    private static final int TICKS_UNTIL_WARNING = 100; // 5 seconds
    private static final int TICKS_AFTER_WARNING = 200; // 10 seconds
    private static final Map<UUID, Integer> playerLowLightTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerWarned = new HashMap<>();
    private static final Map<UUID, Boolean> playerDurationReduced = new HashMap<>();

    public static void register() {
        LOGGER.info("Registering LightLevelTracker");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MurkConfig config = MurkConfig.getInstance();
            int lightThreshold = config.lightThreshold;
            int litAreaDurationTicks = (int) (config.litAreaEffectDuration * 20); // Convert seconds to ticks

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getBlockPos();

                // Check if player is in an allowed dimension
                Identifier dimensionId = world.getRegistryKey().getValue();
                if (!config.dimensions.contains(dimensionId.toString())) {
                    resetPlayer(playerId);
                    continue;
                }

                // Check total light level (block + sky) at player's position
                int lightLevel = world.getLightLevel(pos);
                LOGGER.debug("Player {} at {} in dimension {} has light level {}",
                        player.getName().getString(), pos, dimensionId, lightLevel);

                if (lightLevel < lightThreshold) {
                    // Player is in low light
                    if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        // Increment tick counter if effect not yet applied
                        int ticks = playerLowLightTicks.getOrDefault(playerId, 0) + 1;
                        playerLowLightTicks.put(playerId, ticks);

                        // Send a warning message after 5 seconds if enabled
                        if (config.enableWarningText && ticks >= TICKS_UNTIL_WARNING && !playerWarned.getOrDefault(playerId, false)) {
                            player.sendMessage(
                                    Text.literal("Something lurks in the dark....").styled(style -> style.withColor(0xFF5555)),
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
                            LOGGER.info("Applied MurksGraspEffect (infinite) to player {}", player.getName().getString());
                            resetPlayer(playerId); // Reset timer after applying effect
                        }
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
                        // Remove and reapply Blindness effect with configured duration
                        player.removeStatusEffect(StatusEffects.BLINDNESS);
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.BLINDNESS,
                                litAreaDurationTicks,
                                0, // Amplifier 0
                                false, // Not ambient
                                false // Hide particles
                        ));
                        playerDurationReduced.put(playerId, true);
                        LOGGER.debug("Reduced MurksGraspEffect and Blindness duration to {} ticks for player {}",
                                litAreaDurationTicks, player.getName().getString());
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