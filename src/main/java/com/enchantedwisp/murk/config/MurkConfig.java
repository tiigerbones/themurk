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
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
    public int general_lightThreshold = 3;

    @Comment("Whether to show warning and effect application messages in chat. Default: true")
    public boolean general_enableWarningText = true;

    @Comment("List of dimension IDs where the effect applies (e.g., \"minecraft:overworld\", \"minecraft:the_nether\"). Default: [\"minecraft:overworld\"]")
    public List<String> general_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld"));

    @Comment("Enable light level checks when the player is underwater. Default: false")
    public boolean general_enableUnderwaterLightCheck = false;

    @Comment("Enable Murk's Grasp effect for players in Creative mode. Default: false")
    public boolean general_affectCreativePlayers = false;

    @Comment("Duration (in seconds) that effects persist after entering a lit area. Default: 4.0")
    @ConfigEntry.Gui.PrefixText
    public double effect_murksGraspPersistenceTime = 4.0;

    @Comment("Whether Murk's Grasp effect also applies Blindness. Default: true")
    public boolean effect_blindnessEnabled = true;

    @Comment("Base damage per interval when Murk's Grasp effect is applied. Default: 0.5")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public float effect_baseDamage = 0.5f;

    @Comment("Maximum damage per interval after 60 seconds of Murk's Grasp effect. Default: 2.0")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 20)
    public float effect_maxDamage = 2.0f;

    @Comment("Interval between damage ticks (in seconds) for Murk's Grasp effect. Default: 3.0")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public double effect_damageInterval = 5.5;

    @Comment("Radius (in blocks) to check for dropped luminescent items (e.g., dropped torches). Default: 5.0")
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public double lightSource_droppedItemRadius = 5.0;

    @Comment("Radius (in blocks) to check for nearby players holding luminescent items. Default: 5.0")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public double lightSource_nearbyPlayerRadius = 5.0;

    @Override
    public void validatePostLoad() {
        TheMurk.LOGGER.info("Validating MurkConfig: dimensions={}, lightThreshold={}",
                general_dimensions, general_lightThreshold);

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
        if (effect_murksGraspPersistenceTime <= 0 || Double.isNaN(effect_murksGraspPersistenceTime) || Double.isInfinite(effect_murksGraspPersistenceTime)) {
            TheMurk.LOGGER.warn("Correcting effect_litAreaEffectDuration: {} to 4.0. Must be positive and finite.", effect_murksGraspPersistenceTime);
            effect_murksGraspPersistenceTime = 4.0;
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
            lightSource_droppedItemRadius = 7.0;
        }
        if (lightSource_nearbyPlayerRadius < 1 || Double.isNaN(lightSource_nearbyPlayerRadius) || Double.isInfinite(lightSource_nearbyPlayerRadius)) {
            TheMurk.LOGGER.warn("Correcting lightSource_nearbyPlayerRadius: {} to 7.0. Must be >= 1.", lightSource_nearbyPlayerRadius);
            lightSource_nearbyPlayerRadius = 7.0;
        }

        TheMurk.LOGGER.info("Validation complete: dimensions={}, lightThreshold={}",
                general_dimensions, general_lightThreshold);
    }
}