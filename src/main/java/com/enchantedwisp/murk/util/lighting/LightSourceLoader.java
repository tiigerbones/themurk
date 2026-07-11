package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SuppressWarnings("deprecation")
public class LightSourceLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");

    @Override
    public Identifier getFabricId() {
        return Identifier.of(TheMurk.MOD_ID, "dynamic_light_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, LightSource> lightSources = LightSource.getLightSources();
        lightSources.clear();

        // listResources was removed in 1.21 — use findResources instead
        Map<Identifier, Resource> resources = manager.findResources(
                "valid_lights",
                id -> id.getNamespace().equals(TheMurk.MOD_ID) && id.getPath().endsWith(".json")
        );

        if (resources.isEmpty()) {
            LOGGER.info("No light source JSON files found in {}/valid_lights/. No light sources will be loaded.", TheMurk.MOD_ID);
            return;
        }

        int loadedCount = 0;

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            LOGGER.debug("Processing light source JSON: {}", id.getPath());

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8))) {

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

                LOGGER.debug("Loaded light source from {}: item={}, luminance={}, water_sensitive={}",
                        id.getPath(), itemId, luminance, waterSensitive);

            } catch (Exception e) {
                LOGGER.error("Failed to process JSON {}: {}", id.getPath(), e.getMessage());
            }
        }

        LOGGER.info("Light source loading complete. Total items registered: {}", loadedCount);
    }
}