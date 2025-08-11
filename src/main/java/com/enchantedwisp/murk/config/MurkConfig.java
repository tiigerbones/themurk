package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Config(name = "murk")
public class MurkConfig implements ConfigData {
    @Comment("Light level below which Murk's Grasp effect is triggered (0-15). Default: 3")
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
    public int general_lightThreshold = 3;

    @Comment("Whether to show warning and effect application messages in the action bar. Default: true")
    public boolean general_enableWarningText = true;

    @Comment("Time in seconds before the warning message appears in low light. Default: 5.0")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
    public double general_warningMessageDelay = 5.0;

    @Comment("Time in seconds after the warning message until Murk's Grasp is applied. Default: 10.0")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
    public double general_effectDelayAfterWarning = 10.0;

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

    @Comment("Time in seconds between damage ticks from Murk's Grasp. Default: 5.5")
    public double effect_damageInterval = 5.5;

    @Comment("Radius (in blocks) to detect dropped light-emitting items. Default: 5.0")
    @ConfigEntry.Gui.PrefixText
    public double lightSource_droppedItemRadius = 5.0;

    @Comment("Radius (in blocks) to detect light from nearby players' items. Default: 5.0")
    public double lightSource_nearbyPlayerRadius = 5.0;

    @Override
    public void validatePostLoad() {
        // General validation
        if (general_lightThreshold < 0 || general_lightThreshold > 15) {
            TheMurk.LOGGER.warn("Correcting general_lightThreshold: {} to {}. Must be between 0 and 15.", general_lightThreshold, Math.max(0, Math.min(15, general_lightThreshold)));
            general_lightThreshold = Math.max(0, Math.min(15, general_lightThreshold));
        }
        general_dimensions = general_dimensions.stream()
                .filter(dim -> dim != null && !dim.trim().isEmpty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (general_dimensions.isEmpty()) {
            TheMurk.LOGGER.warn("Correcting general_dimensions: {} to [\"minecraft:overworld\"]. Must not be empty.", general_dimensions);
            general_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld"));
        } else {
            // Remove duplicates while preserving order
            LinkedHashSet<String> uniqueDims = new LinkedHashSet<>(general_dimensions);
            if (uniqueDims.size() < general_dimensions.size()) {
                TheMurk.LOGGER.info("Removed duplicates from general_dimensions. Original: {}, Unique: {}", general_dimensions, uniqueDims);
            }
            general_dimensions = new ArrayList<>(uniqueDims);
        }

        // Effect validation
        if (effect_murksGraspPersistenceTime < 0 || Double.isNaN(effect_murksGraspPersistenceTime) || Double.isInfinite(effect_murksGraspPersistenceTime)) {
            TheMurk.LOGGER.warn("Correcting effect_murksGraspPersistenceTime: {} to 4.0. Must be positive and finite.", effect_murksGraspPersistenceTime);
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
            TheMurk.LOGGER.warn("Correcting effect_damageInterval: {} to 5.5. Must be >= 1.", effect_damageInterval);
            effect_damageInterval = 5.5;
        }

        // LightSource validation
        if (lightSource_droppedItemRadius < 1 || Double.isNaN(lightSource_droppedItemRadius) || Double.isInfinite(lightSource_droppedItemRadius)) {
            TheMurk.LOGGER.warn("Correcting lightSource_droppedItemRadius: {} to 5.0. Must be >= 1.", lightSource_droppedItemRadius);
            lightSource_droppedItemRadius = 5.0;
        }
        if (lightSource_nearbyPlayerRadius < 1 || Double.isNaN(lightSource_nearbyPlayerRadius) || Double.isInfinite(lightSource_nearbyPlayerRadius)) {
            TheMurk.LOGGER.warn("Correcting lightSource_nearbyPlayerRadius: {} to 5.0. Must be >= 1.", lightSource_nearbyPlayerRadius);
            lightSource_nearbyPlayerRadius = 5.0;
        }
    }
}