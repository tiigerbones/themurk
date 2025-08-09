package com.enchantedwisp.murk.util.lighting;

import java.util.HashMap;
import java.util.Map;

public class LightSource {
    private static final Map<String, LightSource> LIGHT_SOURCES = new HashMap<>();
    public final int luminance;
    public final boolean waterSensitive;

    public LightSource(int luminance, boolean waterSensitive) {
        this.luminance = luminance;
        this.waterSensitive = waterSensitive;
    }

    /**
     * Returns the map of registered light sources.
     */
    public static Map<String, LightSource> getLightSources() {
        return LIGHT_SOURCES;
    }
}