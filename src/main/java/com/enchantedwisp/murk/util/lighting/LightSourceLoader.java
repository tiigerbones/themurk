package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class LightSourceLoader implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");

    @Override
    public Identifier getFabricId() {
        return new Identifier(TheMurk.MOD_ID, "dynamic_light_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, LightSource> lightSources = LightSource.getLightSources();
        lightSources.clear();
        String path = "assets/" + TheMurk.MOD_ID + "/dynamiclights/item/";

        Map<Identifier, net.minecraft.resource.Resource> resources = manager.findResources(
                "dynamiclights/item",
                id -> id.getNamespace().equals(TheMurk.MOD_ID) && id.getPath().endsWith(".json")
        );

        if (resources.isEmpty()) {
            LOGGER.info("No light source JSON files found in {}. No light sources will be loaded.", path);
            return;
        }

        int loadedCount = 0;
        for (Map.Entry<Identifier, net.minecraft.resource.Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            LOGGER.info("Processing light source JSON: {}", id.getPath());
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8))) {

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) content.append(line);

                Jankson jankson = Jankson.builder().build();
                JsonObject json = jankson.load(content.toString());

                JsonArray itemsArray = null;
                Integer luminance = null;
                Boolean waterSensitive = json.get(Boolean.class, "water_sensitive");
                if (waterSensitive == null) waterSensitive = false;

                // Optional: silence errors flag
                Boolean silenceError = json.get(Boolean.class, "silence_error");
                if (silenceError == null) silenceError = false;

                // Format detection and normalization
                if (json.containsKey("match")) {
                    JsonObject match = json.getObject("match");
                    Object itemsObj = match.get("items");
                    itemsArray = new JsonArray();
                    if (itemsObj instanceof JsonArray) {
                        itemsArray = (JsonArray) itemsObj;
                    } else if (itemsObj instanceof JsonPrimitive) {
                        itemsArray.add((JsonPrimitive) itemsObj);
                    }

                    Object lum = json.get("luminance");
                    if (lum instanceof JsonPrimitive) {
                        luminance = json.get(Integer.class, "luminance");
                    } else if (lum instanceof JsonObject) {
                        luminance = mapLuminanceType(json.getObject("luminance"));
                    }

                } else if (json.containsKey("item")) {
                    itemsArray = new JsonArray();
                    itemsArray.add(new JsonPrimitive(Objects.requireNonNull(json.get(String.class, "item"))));

                    Object lum = json.get("luminance");
                    if (lum instanceof JsonPrimitive) {
                        try {
                            luminance = Integer.parseInt(((JsonPrimitive) lum).asString());
                        } catch (NumberFormatException e) {
                            luminance = mapLuminanceType(json.getObject("luminance"));
                        }
                    } else if (lum instanceof JsonObject) {
                        luminance = mapLuminanceType(json.getObject("luminance"));
                    }
                } else {
                    if (!silenceError) LOGGER.warn("Skipping {}: Unrecognized format", id.getPath());
                    continue;
                }

                if (itemsArray == null || itemsArray.isEmpty()) {
                    if (!silenceError) LOGGER.warn("Skipping {}: No valid items found", id.getPath());
                    continue;
                }
                if (luminance == null || luminance < 0 || luminance > 15) {
                    if (!silenceError) LOGGER.warn("Skipping {}: Invalid luminance {} (must be 0–15)", id.getPath(), luminance);
                    continue;
                }

                StringBuilder loadedItems = new StringBuilder();
                for (int i = 0; i < itemsArray.size(); i++) {
                    Object obj = itemsArray.get(i);
                    if (!(obj instanceof JsonPrimitive)) continue;
                    String itemId = ((JsonPrimitive) obj).asString().trim();
                    if (itemId.isEmpty()) continue;
                    lightSources.put(itemId, new LightSource(luminance, waterSensitive));
                    loadedItems.append(itemId).append(", ");
                    loadedCount++;
                }

                if (loadedItems.length() > 0) {
                    // Remove trailing comma and space
                    loadedItems.setLength(loadedItems.length() - 2);
                    LOGGER.info("Loaded from {}: items=[{}], luminance={}, water_sensitive={}",
                            id.getPath(), loadedItems.toString(), luminance, waterSensitive);
                } else {
                    LOGGER.info("No items loaded from {} (empty after processing)", id.getPath());
                }

            } catch (Exception e) {
                LOGGER.error("Failed to process JSON {}: {}", id.getPath(), e.getMessage());
            }
        }
        LOGGER.info("Light source loading complete. Total unique items registered: {}", loadedCount);
    }

    private Integer mapLuminanceType(JsonObject lum) {
        if (lum == null || !lum.containsKey("type")) return null;
        String type = lum.get(String.class, "type");
        switch (type) {
            case "block":
                String blockId = lum.get(String.class, "block");
                return getBlockLuminance(blockId);
            case "block_self":
                return getSelfBlockLuminance(lum);
            default:
                return null;
        }
    }

    private Integer getBlockLuminance(String blockId) {
        Block block = Registries.BLOCK.get(new Identifier(blockId));
        if (block == null || block == Blocks.AIR) return null;
        return block.getDefaultState().getLuminance();
    }

    private Integer getSelfBlockLuminance(JsonObject lum) {
        // Attempt to resolve from item to block
        // Example: For "minecraft:torch", this returns torch block luminance
        try {
            String itemId = lum.get(String.class, "block"); // assuming block key if needed
            Item item = Registries.ITEM.get(new Identifier(itemId));
            Block block = Block.getBlockFromItem(item);
            if (block == null || block == Blocks.AIR) return null;
            return block.getDefaultState().getLuminance();
        } catch (Exception e) {
            return null;
        }
    }
}
