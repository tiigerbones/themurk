package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicLightingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");
    private static final Map<String, Integer> ITEM_LIGHT_LEVELS = new HashMap<>();
    private static boolean isDynamicLightingModLoaded = false;

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");

        // Check for LambDynamicLights or Sodium Dynamic Lights
        isDynamicLightingModLoaded = isLambDynamicLightsLoaded() || isSodiumDynamicLightsLoaded();
        LOGGER.info("Dynamic lighting mod detected: {}", isDynamicLightingModLoaded ? "Yes" : "No");

        // Load light_sources.json
        try (InputStream input = DynamicLightingHandler.class.getClassLoader()
                .getResourceAsStream("assets/murk/dynamiclights/item/light_sources.json")) {
            if (input == null) {
                LOGGER.error("Failed to find light_sources.json");
                return;
            }
            JsonArray jsonArray = JsonHelper.deserialize(new Gson(), new InputStreamReader(input), JsonArray.class);
            for (var element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                String itemId = obj.getAsJsonObject("item").get("id").getAsString();
                int luminance = obj.get("luminance").getAsInt();
                if (luminance < 0 || luminance > 15) {
                    LOGGER.warn("Invalid luminance {} for item {}. Must be between 0 and 15. Skipping.", luminance, itemId);
                    continue;
                }
                ITEM_LIGHT_LEVELS.put(itemId, luminance);
                LOGGER.info("Loaded light source: {} with luminance {}", itemId, luminance);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load light_sources.json", e);
        }
    }

    public static int getPlayerLightLevel(PlayerEntity player) {
        MurkConfig config;
        try {
            config = AutoConfig.getConfigHolder(MurkConfig.class).getConfig();
        } catch (Exception e) {
            LOGGER.error("Failed to load MurkConfig, using default values", e);
            config = new MurkConfig();
        }

        // Skip light checks if underwater and not enabled
        if (!config.enableUnderwaterLightCheck && player.isSubmergedInWater()) {
            return 0;
        }

        if (!config.enableDynamicLighting || !isDynamicLightingModLoaded) {
            return 0;
        }

        int maxLightLevel = 0;

        // Check main and off-hand
        for (ItemStack stack : new ItemStack[]{player.getMainHandStack(), player.getOffHandStack()}) {
            if (!stack.isEmpty()) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                Integer lightLevel = ITEM_LIGHT_LEVELS.get(itemId);
                if (lightLevel != null && lightLevel > maxLightLevel) {
                    maxLightLevel = lightLevel;
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
                        if (lightLevel != null && lightLevel > maxLightLevel) {
                            maxLightLevel = lightLevel;
                        }
                    }
                }
            }
        }

        LOGGER.debug("Player {} light level: {}", player.getName().getString(), maxLightLevel);
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