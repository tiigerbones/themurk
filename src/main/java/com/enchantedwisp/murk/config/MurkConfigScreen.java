package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class MurkConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("The Murk Config"))
                .setSavingRunnable(TheMurk::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        MurkConfig config = TheMurk.getConfig();

        // General Category
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enable Dynamic Light Support"),
                        config.general_enableDynamicLightSupport)
                .setTooltip(Component.nullToEmpty("Enable server-side support for light-emitting items (held, dropped, nearby players) based on JSON definitions.\n" +
                        "This counts item light levels for the darkness effect, independent of client-side dynamic light mods."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableDynamicLightSupport = value)
                .build());

        general.addEntry(entryBuilder.startIntSlider(
                        Component.literal("Light Threshold"),
                        config.general_lightThreshold,
                        0, 15)
                .setTooltip(Component.nullToEmpty("Light level below which Murk's Grasp effect triggers.\nMust be between 0 and 15."))
                .setDefaultValue(3)
                .setSaveConsumer(value -> config.general_lightThreshold = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enable Warning Text"),
                        config.general_enableWarningText)
                .setTooltip(Component.nullToEmpty("Show warning messages in the action bar when nearing darkness or when Murk's Grasp is applied."))
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.general_enableWarningText = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Warning Delay (seconds)"),
                        config.general_warningMessageDelay)
                .setTooltip(Component.nullToEmpty("Time in seconds before the warning message appears in low light. Default: 5.0"))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(60.0)
                .setSaveConsumer(value -> config.general_warningMessageDelay = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Effect Delay After Warning (seconds)"),
                        config.general_effectDelayAfterWarning)
                .setTooltip(Component.nullToEmpty("Time in seconds after the warning message until Murk's Grasp is applied. Default: 10.0"))
                .setDefaultValue(10.0)
                .setMin(1.0)
                .setMax(60.0)
                .setSaveConsumer(value -> config.general_effectDelayAfterWarning = value)
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Component.literal("Dimensions"),
                        config.general_dimensions)
                .setTooltip(Component.nullToEmpty("Dimensions where Murk's Grasp applies.\nUse IDs like \"minecraft:overworld\", \"minecraft:the_nether\", \"minecraft:the_end\".\n" +
                        "Available dimensions: minecraft:overworld, minecraft:the_nether, minecraft:the_end"))
                .setDefaultValue(new ArrayList<>(Arrays.asList("minecraft:overworld")))
                .setSaveConsumer(value -> config.general_dimensions = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Use Biome Whitelist"),
                        config.general_useBiomeWhitelist)
                .setTooltip(Component.nullToEmpty("If true, Murk's Grasp only applies in biomes listed in Biome Whitelist.\nIf false, it applies everywhere except biomes in Biome Blacklist."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_useBiomeWhitelist = value)
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Component.literal("Biome Blacklist"),
                        config.general_biomeBlacklist)
                .setTooltip(Component.nullToEmpty("Biomes where Murk's Grasp is disabled when Use Biome Whitelist is false.\n" +
                        "Use IDs like \"minecraft:deep_dark\", \"minecraft:plains\".\n" +
                        "Examples: minecraft:deep_dark, minecraft:plains, minecraft:desert, minecraft:forest"))
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(value -> config.general_biomeBlacklist = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Component.literal("Biome Whitelist"),
                        config.general_biomeWhitelist)
                .setTooltip(Component.nullToEmpty("Biomes where Murk's Grasp is enabled when Use Biome Whitelist is true.\n" +
                        "Use IDs like \"minecraft:plains\", \"minecraft:forest\".\n" +
                        "Examples: minecraft:plains, minecraft:desert, minecraft:forest, minecraft:swamp"))
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(value -> config.general_biomeWhitelist = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enable Underwater Light Check"),
                        config.general_enableUnderwaterLightCheck)
                .setTooltip(Component.nullToEmpty("Enable light level checks when the player is underwater."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableUnderwaterLightCheck = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Affect Creative Players"),
                        config.general_affectCreativePlayers)
                .setTooltip(Component.nullToEmpty("Apply Murk's Grasp effect to players in Creative mode."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_affectCreativePlayers = value)
                .build());

        // Effect Category
        ConfigCategory effect = builder.getOrCreateCategory(Component.literal("Effect"));
        effect.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Effect Persistence Time (seconds)"),
                        config.effect_murksGraspPersistenceTime)
                .setTooltip(Component.nullToEmpty("Duration (in seconds) that effects persist after entering a lit area."))
                .setDefaultValue(4.0)
                .setMin(0.0)
                .setSaveConsumer(value -> config.effect_murksGraspPersistenceTime = value)
                .build());

        effect.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enable Blindness"),
                        config.effect_blindnessEnabled)
                .setTooltip(Component.nullToEmpty("Apply Blindness effect alongside Murk's Grasp."))
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.effect_blindnessEnabled = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Component.literal("Base Damage"),
                        config.effect_baseDamage)
                .setTooltip(Component.nullToEmpty("Initial damage per interval from Murk's Grasp (in half-hearts)."))
                .setDefaultValue(3.5f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_baseDamage = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Component.literal("Max Damage"),
                        config.effect_maxDamage)
                .setTooltip(Component.nullToEmpty("Maximum damage per interval after 60 seconds (in half-hearts)."))
                .setDefaultValue(6.0f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_maxDamage = value)
                .build());

        effect.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Damage Interval (seconds)"),
                        config.effect_damageInterval)
                .setTooltip(Component.nullToEmpty("Time between damage ticks from Murk's Grasp (in seconds)."))
                .setDefaultValue(4.5)
                .setMin(1.0)
                .setSaveConsumer(value -> config.effect_damageInterval = value)
                .build());

        // Light Source Category
        ConfigCategory lightSource = builder.getOrCreateCategory(Component.literal("Light Source"));
        lightSource.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Dropped Item Radius (blocks)"),
                        config.lightSource_droppedItemRadius)
                .setTooltip(Component.nullToEmpty("Radius (in blocks) to detect dropped light-emitting items."))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_droppedItemRadius = value)
                .build());

        lightSource.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Nearby Player Radius (blocks)"),
                        config.lightSource_nearbyPlayerRadius)
                .setTooltip(Component.nullToEmpty("Radius (in blocks) to detect light from nearby players' items."))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_nearbyPlayerRadius = value)
                .build());

        return builder.build();
    }
}