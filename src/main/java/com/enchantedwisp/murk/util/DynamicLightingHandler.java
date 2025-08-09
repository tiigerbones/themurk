package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonArray;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicLightingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");
    private static final Map<String, LightSourceEntry> LIGHT_SOURCES = new HashMap<>();
    private static boolean isDynamicLightingModLoaded = false;

    public static class LightSourceEntry {
        public final int luminance;
        public final boolean waterSensitive;

        public LightSourceEntry(int luminance, boolean waterSensitive) {
            this.luminance = luminance;
            this.waterSensitive = waterSensitive;
        }
    }

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");

        // Check for LambDynamicLights or Sodium Dynamic Lights
        isDynamicLightingModLoaded = isLambDynamicLightsLoaded() || isSodiumDynamicLightsLoaded();
        LOGGER.info("Dynamic lighting mod detected: {}", isDynamicLightingModLoaded ? "Yes" : "No");

        if (isDynamicLightingModLoaded) {
            // Listen for resource reloads so resource pack overrides are respected
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                    new SimpleSynchronousResourceReloadListener() {
                        @Override
                        public Identifier getFabricId() {
                            return new Identifier(TheMurk.MOD_ID, "dynamic_light_loader");
                        }

                        @Override
                        public void reload(ResourceManager manager) {
                            loadLightSources(manager);
                        }
                    }
            );
        }
    }

    private static void loadLightSources(ResourceManager resourceManager) {
        LIGHT_SOURCES.clear();
        String path = "assets/murk/dynamiclights/item/";
        Map<Identifier, net.minecraft.resource.Resource> resources = resourceManager.findResources(
                "dynamiclights/item",
                id -> id.getNamespace().equals(TheMurk.MOD_ID) && id.getPath().endsWith(".json")
        );

        if (resources.isEmpty()) {
            LOGGER.info("No light source JSON files found in {}. No light sources will be loaded.", path);
            return;
        }

        for (Map.Entry<Identifier, net.minecraft.resource.Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }

                Jankson jankson = Jankson.builder().build();
                JsonObject json = jankson.load(jsonContent.toString());

                // Parse "match" field
                JsonObject match = json.getObject("match");
                if (match == null || !match.containsKey("items")) {
                    LOGGER.warn("Skipping {}: Missing or invalid 'match' field", id.getPath());
                    continue;
                }

                Object itemsObj = match.get("items");
                JsonArray itemsArray;
                if (itemsObj instanceof JsonArray) {
                    itemsArray = (JsonArray) itemsObj;
                } else if (itemsObj instanceof JsonPrimitive) {
                    itemsArray = new JsonArray();
                    itemsArray.add((JsonPrimitive) itemsObj);
                } else {
                    LOGGER.warn("Skipping {}: 'match.items' must be a string or array", id.getPath());
                    continue;
                }

                // Parse "luminance" and "water_sensitive"
                Integer luminance = json.get(Integer.class, "luminance");
                if (luminance == null || luminance < 0 || luminance > 15) {
                    LOGGER.warn("Skipping {}: Invalid luminance {}. Must be between 0 and 15.", id.getPath(), luminance);
                    continue;
                }

                Boolean waterSensitive = json.get(Boolean.class, "water_sensitive");
                if (waterSensitive == null) {
                    waterSensitive = false; // Default to false if not specified
                }

                // Register each item
                for (int i = 0; i < itemsArray.size(); i++) {
                    Object itemObj = itemsArray.get(i);
                    if (!(itemObj instanceof JsonPrimitive)) {
                        LOGGER.warn("Skipping item in {}: Invalid item ID at index {}", id.getPath(), i);
                        continue;
                    }
                    String itemId = ((JsonPrimitive) itemObj).asString();
                    if (itemId == null || itemId.trim().isEmpty()) {
                        LOGGER.warn("Skipping item in {}: Empty item ID at index {}", id.getPath(), i);
                        continue;
                    }
                    LIGHT_SOURCES.put(itemId, new LightSourceEntry(luminance, waterSensitive));
                    LOGGER.info("Loaded light source from {}: item={}, luminance={}, water_sensitive={}",
                            id.getPath(), itemId, luminance, waterSensitive);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to process JSON file {}: {}", id.getPath(), e.getMessage());
            }
        }
    }

    public static int getPlayerLightLevel(PlayerEntity player) {
        if (!isDynamicLightingModLoaded) {
            LOGGER.debug("Skipping light level check for player {}: No dynamic lighting mod loaded", player.getName().getString());
            return 0;
        }

        MurkConfig config = TheMurk.getConfig();
        if (!config.general_enableUnderwaterLightCheck && player.isSubmergedInWater()) {
            LOGGER.debug("Skipping light level check for player {}: Underwater with general_enableUnderwaterLightCheck=false", player.getName().getString());
            return 0;
        }

        int maxLightLevel = 0;

        // Check main and off-hand
        for (ItemStack stack : new ItemStack[]{player.getMainHandStack(), player.getOffHandStack()}) {
            if (!stack.isEmpty()) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                LightSourceEntry entry = LIGHT_SOURCES.get(itemId);
                if (entry != null) {
                    if (entry.waterSensitive && player.isSubmergedInWater()) {
                        LOGGER.debug("Skipping light source {} for player {}: Water-sensitive and submerged", itemId, player.getName().getString());
                        continue;
                    }
                    maxLightLevel = Math.max(maxLightLevel, entry.luminance);
                    LOGGER.debug("Found light source {} in hand with luminance {} for player {}", itemId, entry.luminance, player.getName().getString());
                }
            }
        }

        // Check trinkets
        if (isTrinketsLoaded()) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
            if (trinketComponent.isPresent()) {
                for (var group : trinketComponent.get().getAllEquipped()) {
                    ItemStack stack = group.getRight();
                    if (!stack.isEmpty()) {
                        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                        LightSourceEntry entry = LIGHT_SOURCES.get(itemId);
                        if (entry != null) {
                            if (entry.waterSensitive && player.isSubmergedInWater()) {
                                LOGGER.debug("Skipping light source {} in trinket for player {}: Water-sensitive and submerged", itemId, player.getName().getString());
                                continue;
                            }
                            maxLightLevel = Math.max(maxLightLevel, entry.luminance);
                            LOGGER.debug("Found light source {} in trinket with luminance {} for player {}", itemId, entry.luminance, player.getName().getString());
                        }
                    }
                }
            }
        }

        LOGGER.debug("Player {} total light level: {}", player.getName().getString(), maxLightLevel);
        return maxLightLevel;
    }

    public static Map<String, LightSourceEntry> getLightSources() {
        return LIGHT_SOURCES;
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