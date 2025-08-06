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

    @Comment("Base damage per interval when Murk's Grasp effect is applied. Default: 0.5")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public float baseDamage = 0.5f;

    @Comment("Maximum damage per interval after 60 seconds of Murk's Grasp effect. Default: 2.0")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 20)
    public float maxDamage = 2.0f;

    @Comment("Interval between damage ticks (in seconds) for Murk's Grasp effect. Default: 3.0")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public double damageInterval = 3.0;

    @Comment("Enable dynamic lighting support for held and trinket items when LambDynamicLights or Sodium Dynamic Lights is installed. Default: true")
    public boolean enableDynamicLighting = true;

    @Comment("Enable light level checks when the player is underwater. Default: false")
    public boolean enableUnderwaterLightCheck = false;

    @Override
    public void validatePostLoad() throws ValidationException {
        // Correct invalid values where possible
        if (lightThreshold < 0 || lightThreshold > 15) {
            TheMurk.LOGGER.warn("Correcting invalid lightThreshold: {}. Must be between 0 and 15.", lightThreshold);
            lightThreshold = Math.max(0, Math.min(15, lightThreshold));
        }
        if (litAreaEffectDuration <= 0 || Double.isNaN(litAreaEffectDuration)) {
            TheMurk.LOGGER.warn("Correcting invalid litAreaEffectDuration: {}. Must be positive.", litAreaEffectDuration);
            litAreaEffectDuration = 4.0;
        }
        if (dimensions == null || dimensions.isEmpty()) {
            TheMurk.LOGGER.warn("Correcting invalid dimensions: {}. Must be a non-empty list.", dimensions);
            dimensions = Arrays.asList("minecraft:overworld");
        }
        if (baseDamage < 0 || baseDamage > 10) {
            TheMurk.LOGGER.warn("Correcting invalid baseDamage: {}. Must be between 0 and 10.", baseDamage);
            baseDamage = Math.max(0, Math.min(10, baseDamage));
        }
        if (maxDamage < baseDamage || maxDamage > 20) {
            TheMurk.LOGGER.warn("Correcting invalid maxDamage: {}. Must be >= baseDamage and <= 20.", maxDamage);
            maxDamage = Math.max(baseDamage, Math.min(20, maxDamage));
        }
        if (damageInterval < 1 || damageInterval > 10 || Double.isNaN(damageInterval)) {
            TheMurk.LOGGER.warn("Correcting invalid damageInterval: {}. Must be between 1 and 10.", damageInterval);
            damageInterval = 3.0;
        }
    }
}