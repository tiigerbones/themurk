package com.enchantedwisp.murk.util.tracker;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.core.PhaseManager;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightLevelMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_monitor");

    public static void register() {
        LOGGER.info("Registering LightLevelMonitor");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getServerWorld();
                UUID id = player.getUuid();

                // Always clear flags if effect expired (runs regardless of env)
                if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    PlayerLightTracker.clearDurationReduced(id);
                    PlayerLightTracker.clearEffectTicks(id);
                }

                // Unified invalid env check (biome, dimension, creative, underwater)
                boolean isValidEnv = isValidEnvironment(player, world);
                if (!isValidEnv) {
                    boolean hasGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);
                    if (!hasGrasp) {
                        // Invalid env + no effect: Reset and skip (SAFE default)
                        PlayerLightTracker.reset(id);
                        PhaseManager.resetPhase(id);
                        continue;
                    } else {
                        // Invalid env + has effect: Fake 'lit' to trigger RECOVERY persistence
                        int fakeLight = ConfigCache.getLightThreshold();
                        LOGGER.debug("Invalid env for {}: Faking light={} to enter RECOVERY", player.getName().getString(), fakeLight);
                        PhaseManager.updatePlayer(player, world, fakeLight);
                        continue;
                    }
                }

                // Valid env: Use real light level
                int lightLevel = LightLevelEvaluator.getEffectiveLightLevel(player);
                PhaseManager.updatePlayer(player, world, lightLevel);
            }
        });
    }

    /**
     * Checks if the player's environment is valid for Murk effects.
     * Invalid: Black/whitelisted biome, disallowed dimension, creative (disabled), underwater (disabled).
     */
    private static boolean isValidEnvironment(ServerPlayerEntity player, ServerWorld world) {
        // Biome check
        Identifier biomeId = world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getId(world.getBiome(player.getBlockPos()).value());
        boolean biomeValid;
        if (ConfigCache.useBiomeWhitelist()) {
            biomeValid = biomeId != null && ConfigCache.getBiomeWhitelist().contains(biomeId.toString());
            if (!biomeValid && ConfigCache.getBiomeWhitelist().isEmpty()) {
                LOGGER.debug("Empty biome whitelist—skipping player {} in {}", player.getName().getString(), biomeId);
            }
        } else {
            biomeValid = biomeId == null || !ConfigCache.getBiomeBlacklist().contains(biomeId.toString());
        }
        if (!biomeValid) return false;

        // Dimension check
        String dimStr = world.getRegistryKey().getValue().toString();
        if (!ConfigCache.getAllowedDimensions().contains(dimStr)) {
            LOGGER.debug("Invalid dimension {} for player {}", dimStr, player.getName().getString());
            return false;
        }

        // Creative check
        if (player.isCreative() && !ConfigCache.isCreativeEffectEnabled()) {
            LOGGER.debug("Creative mode disabled for player {}", player.getName().getString());
            return false;
        }

        // Underwater check
        if (!ConfigCache.isUnderwaterLightCheckEnabled() && player.isSubmergedInWater()) {
            LOGGER.debug("Underwater check disabled for player {}", player.getName().getString());
            return false;
        }

        return true;
    }
}