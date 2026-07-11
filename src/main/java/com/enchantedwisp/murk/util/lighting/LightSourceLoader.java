package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LightSourceLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(TheMurk.MOD_ID, "dynamic_light_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Map<String, LightSource> lightSources = LightSource.getLightSources();
        lightSources.clear();

        Map<ResourceLocation, Resource> resources = manager.listResources(
                "valid_lights",
                id -> id.getNamespace().equals(TheMurk.MOD_ID) && id.getPath().endsWith(".json")
        );

        if (resources.isEmpty()) {
            LOGGER.info("No light source JSON files found in {}/validlights/. No light sources will be loaded.", TheMurk.MOD_ID);
            return;
        }

        int loadedCount = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            LOGGER.info("Processing light source JSON: {}", id.getPath());

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8))) {

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }

                Jankson jankson = Jankson.builder().build();
                JsonObject json = jankson.load(content.toString());

                String itemId = json.get(String.class, "item");
                if (itemId == null || itemId.trim().isEmpty()) {
                    LOGGER.warn("Skipping {}: Missing or empty 'item' field", id.getPath());
                    continue;
                }

                Integer luminance = json.get(Integer.class, "luminance");
                if (luminance == null) {
                    // Fallback: try reading as string then parse (in case someone writes it as "15")
                    String lumStr = json.get(String.class, "luminance");
                    if (lumStr != null) {
                        try {
                            luminance = Integer.parseInt(lumStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (luminance == null || luminance < 0 || luminance > 15) {
                    LOGGER.warn("Skipping {}: Invalid luminance {} (must be integer 0–15)", id.getPath(), luminance);
                    continue;
                }

                Boolean waterSensitive = json.get(Boolean.class, "water_sensitive");
                if (waterSensitive == null) {
                    waterSensitive = false;
                }

                lightSources.put(itemId, new LightSource(luminance, waterSensitive));
                loadedCount++;

                LOGGER.info("Loaded light source from {}: item={}, luminance={}, water_sensitive={}",
                        id.getPath(), itemId, luminance, waterSensitive);

            } catch (Exception e) {
                LOGGER.error("Failed to process JSON {}: {}", id.getPath(), e.getMessage());
            }
        }

        LOGGER.info("Light source loading complete. Total items registered: {}", loadedCount);
    }
}