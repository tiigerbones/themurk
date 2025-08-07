package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Config(name = "murk")
public class MurkConfig implements ConfigData {
    @Comment("Light level below which Murk's Grasp effect is triggered (0-15). Default: 3")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
    public int general_lightThreshold = 3;

    @Comment("Whether to show warning and effect application messages in chat. Default: true")
    @ConfigEntry.Gui.Tooltip
    public boolean general_enableWarningText = true;

    @Comment("List of dimension IDs where the effect applies (e.g., \"minecraft:overworld\", \"minecraft:the_nether\"). Default: [\"minecraft:overworld\"]")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<String> general_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld"));

    @Comment("Enable light level checks when the player is underwater. Default: false")
    @ConfigEntry.Gui.Tooltip
    public boolean general_enableUnderwaterLightCheck = false;

    @Comment("Enable Murk's Grasp effect for players in Creative mode. Default: false")
    @ConfigEntry.Gui.Tooltip
    public boolean general_enableCreativeEffect = false;

    @Comment("Duration (in seconds) that effects persist after entering a lit area. Default: 4.0")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public double effect_litAreaEffectDuration = 4.0;

    @Comment("Whether Murk's Grasp effect also applies Blindness. Default: true")
    @ConfigEntry.Gui.Tooltip
    public boolean effect_blindnessEnabled = true;

    @Comment("Base damage per interval when Murk's Grasp effect is applied. Default: 0.5")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public float effect_baseDamage = 0.5f;

    @Comment("Maximum damage per interval after 60 seconds of Murk's Grasp effect. Default: 2.0")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 20)
    public float effect_maxDamage = 2.0f;

    @Comment("Interval between damage ticks (in seconds) for Murk's Grasp effect. Default: 3.0")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public double effect_damageInterval = 3.0;

    @Comment("Radius (in blocks) to check for dropped luminescent items (e.g., dropped torches). Default: 7.0")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public double lightSource_droppedItemRadius = 5.0;

    @Comment("Radius (in blocks) to check for nearby players holding luminescent items. Default: 7.0")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public double lightSource_nearbyPlayerRadius = 5.0;

    @Comment("List of items that emit light when held, in trinket slots, dropped, or held by nearby players. Format: {id: \"minecraft:torch\", luminance: 14, water_sensitive: true} Make sure to create the correct json file in assets/murk/dynamiclights/item so the item actually emits light visually")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<LightSourceEntry> lightSource_lightSources = new ArrayList<>(Arrays.asList(
            new LightSourceEntry("minecraft:torch", 14, true),
            new LightSourceEntry("minecraft:lantern", 15, false),
            new LightSourceEntry("minecraft:glowstone", 15, false)
    ));

    public static class LightSourceEntry {
        @ConfigEntry.Gui.Tooltip
        public String id;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
        public int luminance;
        @ConfigEntry.Gui.Tooltip
        public boolean waterSensitive;

        public LightSourceEntry() {
            // Required for deserialization
        }

        public LightSourceEntry(String id, int luminance, boolean waterSensitive) {
            this.id = id;
            this.luminance = luminance;
            this.waterSensitive = waterSensitive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LightSourceEntry)) return false;
            LightSourceEntry other = (LightSourceEntry) o;
            return id.equals(other.id) && luminance == other.luminance && waterSensitive == other.waterSensitive;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    @Override
    public void validatePostLoad() {
        TheMurk.LOGGER.info("Validating MurkConfig: dimensions={}, lightThreshold={}, lightSources={}",
                general_dimensions, general_lightThreshold, lightSource_lightSources);

        // General validation
        if (general_lightThreshold < 0 || general_lightThreshold > 15) {
            TheMurk.LOGGER.warn("Correcting general_lightThreshold: {} to {}. Must be between 0 and 15.", general_lightThreshold, Math.max(0, Math.min(15, general_lightThreshold)));
            general_lightThreshold = Math.max(0, Math.min(15, general_lightThreshold));
        }
        if (general_dimensions == null) {
            TheMurk.LOGGER.warn("general_dimensions is null. Setting to default: [\"minecraft:overworld\"]");
            general_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld"));
        } else {
            List<String> original = new ArrayList<>(general_dimensions);
            general_dimensions = general_dimensions.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
            if (general_dimensions.isEmpty()) {
                TheMurk.LOGGER.warn("general_dimensions is empty after cleanup. Setting to default: [\"minecraft:overworld\"]");
                general_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld"));
            } else if (!general_dimensions.equals(original)) {
                TheMurk.LOGGER.info("Deduplicated general_dimensions: {} to {}", original, general_dimensions);
            }
        }

        // Effect validation
        if (effect_litAreaEffectDuration <= 0 || Double.isNaN(effect_litAreaEffectDuration) || Double.isInfinite(effect_litAreaEffectDuration)) {
            TheMurk.LOGGER.warn("Correcting effect_litAreaEffectDuration: {} to 4.0. Must be positive and finite.", effect_litAreaEffectDuration);
            effect_litAreaEffectDuration = 4.0;
        }
        if (effect_baseDamage < 0 || Float.isNaN(effect_baseDamage)) {
            TheMurk.LOGGER.warn("Correcting effect_baseDamage: {} to {}. Must be non-negative.", effect_baseDamage, Math.max(0, effect_baseDamage));
            effect_baseDamage = Math.max(0, effect_baseDamage);
        }
        if (effect_maxDamage < effect_baseDamage || Float.isNaN(effect_maxDamage)) {
            TheMurk.LOGGER.warn("Correcting effect_maxDamage: {} to {}. Must be >= baseDamage.", effect_maxDamage, Math.max(effect_baseDamage, effect_maxDamage));
            effect_maxDamage = Math.max(effect_baseDamage, effect_maxDamage);
        }
        if (effect_damageInterval < 1 || Double.isNaN(effect_damageInterval) || Double.isInfinite(effect_damageInterval)) {
            TheMurk.LOGGER.warn("Correcting effect_damageInterval: {} to 3.0. Must be >= 1.", effect_damageInterval);
            effect_damageInterval = 3.0;
        }

        // LightSource validation
        if (lightSource_droppedItemRadius < 1 || Double.isNaN(lightSource_droppedItemRadius) || Double.isInfinite(lightSource_droppedItemRadius)) {
            TheMurk.LOGGER.warn("Correcting lightSource_droppedItemRadius: {} to 7.0. Must be >= 1.", lightSource_droppedItemRadius);
            lightSource_droppedItemRadius = 5.0;
        }
        if (lightSource_nearbyPlayerRadius < 1 || Double.isNaN(lightSource_nearbyPlayerRadius) || Double.isInfinite(lightSource_nearbyPlayerRadius)) {
            TheMurk.LOGGER.warn("Correcting lightSource_nearbyPlayerRadius: {} to 7.0. Must be >= 1.", lightSource_nearbyPlayerRadius);
            lightSource_nearbyPlayerRadius = 5.0;
        }
        if (lightSource_lightSources == null) {
            TheMurk.LOGGER.warn("lightSource_lightSources is null. Setting to default.");
            lightSource_lightSources = new ArrayList<>(Arrays.asList(
                    new LightSourceEntry("minecraft:torch", 14, true),
                    new LightSourceEntry("minecraft:lantern", 15, false),
                    new LightSourceEntry("minecraft:glowstone", 15, false)
            ));
        } else {
            List<LightSourceEntry> original = new ArrayList<>(lightSource_lightSources);
            lightSource_lightSources = lightSource_lightSources.stream()
                    .filter(source -> source != null && source.id != null && !source.id.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
            for (LightSourceEntry source : lightSource_lightSources) {
                if (source.luminance < 0 || source.luminance > 15) {
                    TheMurk.LOGGER.warn("Correcting luminance for {}: {} to {}. Must be between 0 and 15.", source.id, source.luminance, Math.max(0, Math.min(15, source.luminance)));
                    source.luminance = Math.max(0, Math.min(15, source.luminance));
                }
            }
            if (lightSource_lightSources.isEmpty()) {
                TheMurk.LOGGER.warn("lightSource_lightSources is empty after cleanup. Setting to default.");
                lightSource_lightSources = new ArrayList<>(Arrays.asList(
                        new LightSourceEntry("minecraft:torch", 14, true),
                        new LightSourceEntry("minecraft:lantern", 15, false),
                        new LightSourceEntry("minecraft:glowstone", 15, false)
                ));
            } else if (!lightSource_lightSources.equals(original)) {
                TheMurk.LOGGER.info("Deduplicated lightSource_lightSources: {} to {}", original, lightSource_lightSources);
            }
        }

        TheMurk.LOGGER.info("Validation complete: dimensions={}, lightThreshold={}, lightSources={}",
                general_dimensions, general_lightThreshold, lightSource_lightSources);
    }
}