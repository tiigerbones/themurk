package com.enchantedwisp.murk.util.tracker;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class LightLevelMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_monitor");

    /**
     * Registers the server tick event to monitor light levels and apply effects.
     */
    public static void register() {
        LOGGER.info("Registering LightLevelMonitor");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                ServerWorld world = player.getServerWorld();

                // Check if player is in a blacklisted biome
                Identifier biomeId = world.getRegistryManager()
                        .get(RegistryKeys.BIOME)
                        .getId(world.getBiome(player.getBlockPos()).value());
                if (biomeId != null && ConfigCache.getBiomeBlacklist().contains(biomeId.toString())) {
                    PlayerLightTracker.reset(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (ConfigCache.isBlindnessEnabled()) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                    }
                    continue;
                }

                // Skip Creative mode players unless enabled
                if (player.isCreative() && !ConfigCache.isCreativeEffectEnabled()) {
                    PlayerLightTracker.reset(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (ConfigCache.isBlindnessEnabled()) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                    }
                    continue;
                }

                // Check if player is in an allowed dimension
                Identifier dimensionId = world.getRegistryKey().getValue();
                if (!ConfigCache.getAllowedDimensions().contains(dimensionId.toString())) {
                    PlayerLightTracker.reset(playerId);
                    continue;
                }

                // Skip light checks if underwater and not enabled
                if (!ConfigCache.isUnderwaterLightCheckEnabled() && player.isSubmergedInWater()) {
                    PlayerLightTracker.reset(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (ConfigCache.isBlindnessEnabled()) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                    }
                    continue;
                }

                // Check total light level
                int lightLevel = LightLevelEvaluator.getEffectiveLightLevel(player);
                if (lightLevel < ConfigCache.getLightThreshold()) {
                    // Player is in low light
                    if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        // Increment tick counter if effect not yet applied
                        PlayerLightTracker.incrementLowLightTicks(playerId);
                        int ticks = PlayerLightTracker.getLowLightTicks(playerId);

                        // Send warning message after configured delay if enabled
                        if (ConfigCache.isWarningTextEnabled() && ticks >= ConfigCache.getTicksUntilWarning() && !PlayerLightTracker.isWarned(playerId)) {

                            String fullMessage = "An evil presence lurks in the dark nearby...";
                            int totalFrames = 28; // how many updates total (~totalFrames * frameInterval ticks)
                            int frameInterval = 2; // ticks between updates
                            double glitchDurationRatio = 0.4; // 40% of time is glitchy, last 60% normal

                            final int[] frame = {0};
                            final int[] tickCounter = {0};

                            ServerTickEvents.END_SERVER_TICK.register(serverTick -> {
                                if (frame[0] > totalFrames) {
                                    return; // stop after total time
                                }

                                tickCounter[0]++;
                                if (tickCounter[0] >= frameInterval) {
                                    tickCounter[0] = 0;

                                    boolean isGlitchPhase = frame[0] < totalFrames * glitchDurationRatio;

                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < fullMessage.length(); i++) {
                                        char c = fullMessage.charAt(i);
                                        if (c == ' ') {
                                            sb.append(' ');
                                        } else if (isGlitchPhase && Math.random() < 0.2) {
                                            // 20% chance per character to glitch during glitch phase
                                            sb.append("§k").append(c).append("§r");
                                        } else {
                                            sb.append(c);
                                        }
                                    }

                                    player.sendMessageToClient(
                                            Text.literal(sb.toString()).styled(style -> style.withColor(0xFF5555)),
                                            true
                                    );

                                    frame[0]++;
                                }
                            });

                            PlayerLightTracker.setWarned(playerId, true);
                        }


                        // Apply effect with infinite duration after configured total delay
                        if (ticks >= ConfigCache.getTicksUntilWarning() + ConfigCache.getTicksAfterWarning()) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    Effects.MURKS_GRASP,
                                    -1, // Infinite duration
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    true // Show particles
                            ));
                            if (ConfigCache.isBlindnessEnabled()) {
                                player.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.BLINDNESS,
                                        -1, // Infinite duration
                                        0, // Amplifier 0
                                        false, // Not ambient
                                        false // Hide particles
                                ));
                            }
                            PlayerLightTracker.reset(playerId); // Reset timer after applying effect
                        }
                    }
                } else {
                    // Player is in a lit area
                    if (player.hasStatusEffect(Effects.MURKS_GRASP) && !PlayerLightTracker.isDurationReduced(playerId)) {
                        // Change effect to finite duration
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        player.addStatusEffect(new StatusEffectInstance(
                                Effects.MURKS_GRASP,
                                ConfigCache.getLitAreaDurationTicks(),
                                0, // Amplifier 0
                                false, // Not ambient
                                true // Show particles
                        ));
                        // Remove and reapply Blindness effect with configured duration if enabled
                        if (ConfigCache.isBlindnessEnabled()) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                            player.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.BLINDNESS,
                                    ConfigCache.getLitAreaDurationTicks(),
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    false // Hide particles
                            ));
                        }
                        PlayerLightTracker.setDurationReduced(playerId, true);
                        PlayerLightTracker.resetEffectTicks(playerId);
                    }
                    // Reset timer and warning state
                    PlayerLightTracker.reset(playerId);
                }

                // Clear duration reduction flag and effect ticks if effect expires
                if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    PlayerLightTracker.clearDurationReduced(playerId);
                    PlayerLightTracker.clearEffectTicks(playerId);
                }
            }
        });
    }
}