package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;


import java.util.Arrays;
import java.util.List;

@Config(name = "murk")
public class MurkConfig implements ConfigData {
    @Comment("Light level below which Murk's Grasp effect is triggered (0-15). Default: 3")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
    public int lightThreshold = 3;

    @Comment("Whether to show warning and effect application messages in chat. Default: true")
    public boolean enableWarningText = true;

    @Comment("Duration (in seconds) that effects persist after entering a lit area. Default: 4.0")
    public double litAreaEffectDuration = 4.0;

    @Comment("List of dimension IDs where the effect applies (e.g., \"minecraft:overworld\", \"minecraft:the_nether\"). Default: [\"minecraft:overworld\"]")
    public List<String> dimensions = Arrays.asList("minecraft:overworld");

    @Comment("Whether Murk's Grasp effect also applies Blindness. Default: true")
    public boolean blindnessEnabled = true;

    @Override
    public void validatePostLoad() throws ValidationException {
        // Correct invalid values where possible
        if (lightThreshold < 0 || lightThreshold > 15) {
            TheMurk.LOGGER.warn("Correcting invalid lightThreshold: {}. Must be between 0 and 15.", lightThreshold);
            lightThreshold = Math.max(0, Math.min(15, lightThreshold));
        }
        if (litAreaEffectDuration <= 0 || Double.isNaN(litAreaEffectDuration)) {
            TheMurk.LOGGER.warn("Correcting invalid litAreaEffectDuration: {}. Must be positive.", litAreaEffectDuration);
            litAreaEffectDuration = 4.0; // Reset to default
        }
        if (dimensions == null || dimensions.isEmpty()) {
            TheMurk.LOGGER.warn("Correcting invalid dimensions: {}. Must be a non-empty list.", dimensions);
            dimensions = Arrays.asList("minecraft:overworld");
        }
        if (enableWarningText) {
            TheMurk.LOGGER.warn("Correcting invalid enableWarningText: null. Must be true or false.");
            enableWarningText = true;
        }
        if (blindnessEnabled) {
            TheMurk.LOGGER.warn("Correcting invalid blindnessEnabled: null. Must be true or false.");
            blindnessEnabled = true;
        }
    }
}