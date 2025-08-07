package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicLightingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");
    private static final Map<String, Integer> ITEM_LIGHT_LEVELS = new HashMap<>();
    private static final Map<String, Boolean> ITEM_WATER_SENSITIVE = new HashMap<>();
    private static boolean isDynamicLightingModLoaded = false;

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");

        // Check for LambDynamicLights or Sodium Dynamic Lights
        isDynamicLightingModLoaded = isLambDynamicLightsLoaded() || isSodiumDynamicLightsLoaded();
        LOGGER.info("Dynamic lighting mod detected: {}", isDynamicLightingModLoaded ? "Yes" : "No");

        // Load light sources from cached config
        MurkConfig config = TheMurk.getConfig();
        for (MurkConfig.LightSourceEntry source : config.lightSource_lightSources) {
            if (source == null || source.id == null || source.id.isEmpty()) {
                LOGGER.warn("Skipping invalid light source: Null or empty ID");
                continue;
            }
            if (source.luminance < 0 || source.luminance > 15) {
                LOGGER.warn("Invalid luminance {} for item {}. Must be between 0 and 15. Skipping.", source.luminance, source.id);
                continue;
            }
            ITEM_LIGHT_LEVELS.put(source.id, source.luminance);
            ITEM_WATER_SENSITIVE.put(source.id, source.waterSensitive);
            LOGGER.info("Loaded light source: {} with luminance {}, water_sensitive: {}", source.id, source.luminance, source.waterSensitive);
        }
    }

    public static int getPlayerLightLevel(PlayerEntity player) {
        if (!isDynamicLightingModLoaded) {
            LOGGER.debug("Skipping light level check for player {}: No dynamic lighting mod loaded", player.getName().getString());
            return 0;
        }

        // Check underwater config
        MurkConfig config = TheMurk.getConfig();
        // Skip light checks if underwater and not enabled
        if (!config.general_enableUnderwaterLightCheck && player.isSubmergedInWater()) {
            LOGGER.debug("Skipping light level check for player {}: Underwater with general_enableUnderwaterLightCheck=false", player.getName().getString());
            return 0;
        }

        int maxLightLevel = 0;

        // Check main and off-hand
        for (ItemStack stack : new ItemStack[]{player.getMainHandStack(), player.getOffHandStack()}) {
            if (!stack.isEmpty()) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                Integer lightLevel = ITEM_LIGHT_LEVELS.get(itemId);
                if (lightLevel != null) {
                    // Skip if water-sensitive and player is submerged
                    if (ITEM_WATER_SENSITIVE.getOrDefault(itemId, false) && player.isSubmergedInWater()) {
                        LOGGER.debug("Skipping light source {} for player {}: Water-sensitive and submerged", itemId, player.getName().getString());
                        continue;
                    }
                    if (lightLevel > maxLightLevel) {
                        maxLightLevel = lightLevel;
                        LOGGER.debug("Found light source {} in hand with luminance {} for player {}", itemId, lightLevel, player.getName().getString());
                    }
                }
            }
        }

        // Check trinket slots if Trinkets is installed
        if (isTrinketsLoaded()) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
            if (trinketComponent.isPresent()) {
                for (var group : trinketComponent.get().getAllEquipped()) {
                    ItemStack stack = group.getRight();
                    if (!stack.isEmpty()) {
                        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                        Integer lightLevel = ITEM_LIGHT_LEVELS.get(itemId);
                        if (lightLevel != null) {
                            // Skip if water-sensitive and player is submerged
                            if (ITEM_WATER_SENSITIVE.getOrDefault(itemId, false) && player.isSubmergedInWater()) {
                                LOGGER.debug("Skipping light source {} in trinket for player {}: Water-sensitive and submerged", itemId, player.getName().getString());
                                continue;
                            }
                            if (lightLevel > maxLightLevel) {
                                maxLightLevel = lightLevel;
                                LOGGER.debug("Found light source {} in trinket with luminance {} for player {}", itemId, lightLevel, player.getName().getString());
                            }
                        }
                    }
                }
            }
        }

        LOGGER.debug("Player {} total light level: {}", player.getName().getString(), maxLightLevel);
        return maxLightLevel;
    }

    public static boolean isDynamicLightingModLoaded() {
        return isDynamicLightingModLoaded;
    }

    private static boolean isLambDynamicLightsLoaded() {
        try {
            Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isSodiumDynamicLightsLoaded() {
        try {
            Class.forName("toni.sodiumdynamiclights.SodiumDynamicLights");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isTrinketsLoaded() {
        try {
            Class.forName("dev.emi.trinkets.api.TrinketsApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}