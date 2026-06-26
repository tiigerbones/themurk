package com.enchantedwisp.murk.util.lighting;

import java.util.HashMap;
import java.util.Map;

public record LightSource(int luminance, boolean waterSensitive) {
    private static final Map<String, LightSource> LIGHT_SOURCES = new HashMap<>();

    /**
     * Returns the map of registered light sources.
     */
    public static Map<String, LightSource> getLightSources() {
        return LIGHT_SOURCES;
    }
}