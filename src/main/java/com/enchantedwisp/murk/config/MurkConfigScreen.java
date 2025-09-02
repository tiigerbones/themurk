package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class MurkConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("The Murk Config"))
                .setSavingRunnable(TheMurk::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        MurkConfig config = TheMurk.getConfig();

        // General Category
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Dynamic Light Support"),
                        config.general_enableDynamicLightSupport)
                .setTooltip(Text.of("Enable server-side support for light-emitting items (held, dropped, nearby players) based on JSON definitions.\n" +
                        "This counts item light levels for the darkness effect, independent of client-side dynamic light mods."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableDynamicLightSupport = value)
                .build());

        general.addEntry(entryBuilder.startIntSlider(
                        Text.literal("Light Threshold"),
                        config.general_lightThreshold,
                        0, 15)
                .setTooltip(Text.of("Light level below which Murk's Grasp effect triggers.\nMust be between 0 and 15."))
                .setDefaultValue(3)
                .setSaveConsumer(value -> config.general_lightThreshold = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Warning Text"),
                        config.general_enableWarningText)
                .setTooltip(Text.of("Show warning messages in the action bar when nearing darkness or when Murk's Grasp is applied."))
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.general_enableWarningText = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Warning Delay (seconds)"),
                        config.general_warningMessageDelay)
                .setTooltip(Text.of("Time in seconds before the warning message appears in low light. Default: 5.0"))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(60.0)
                .setSaveConsumer(value -> config.general_warningMessageDelay = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Effect Delay After Warning (seconds)"),
                        config.general_effectDelayAfterWarning)
                .setTooltip(Text.of("Time in seconds after the warning message until Murk's Grasp is applied. Default: 10.0"))
                .setDefaultValue(10.0)
                .setMin(1.0)
                .setMax(60.0)
                .setSaveConsumer(value -> config.general_effectDelayAfterWarning = value)
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Text.literal("Dimensions"),
                        config.general_dimensions)
                .setTooltip(Text.of("Dimensions where Murk's Grasp applies.\nUse IDs like \"minecraft:overworld\", \"minecraft:the_nether\", \"minecraft:the_end\".\n" +
                        "Available dimensions: minecraft:overworld, minecraft:the_nether, minecraft:the_end"))
                .setDefaultValue(new ArrayList<>(Arrays.asList("minecraft:overworld")))
                .setSaveConsumer(value -> config.general_dimensions = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Use Biome Whitelist"),
                        config.general_useBiomeWhitelist)
                .setTooltip(Text.of("If true, Murk's Grasp only applies in biomes listed in Biome Whitelist.\nIf false, it applies everywhere except biomes in Biome Blacklist."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_useBiomeWhitelist = value)
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Text.literal("Biome Blacklist"),
                        config.general_biomeBlacklist)
                .setTooltip(Text.of("Biomes where Murk's Grasp is disabled when Use Biome Whitelist is false.\n" +
                        "Use IDs like \"minecraft:deep_dark\", \"minecraft:plains\".\n" +
                        "Examples: minecraft:deep_dark, minecraft:plains, minecraft:desert, minecraft:forest"))
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(value -> config.general_biomeBlacklist = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Text.literal("Biome Whitelist"),
                        config.general_biomeWhitelist)
                .setTooltip(Text.of("Biomes where Murk's Grasp is enabled when Use Biome Whitelist is true.\n" +
                        "Use IDs like \"minecraft:plains\", \"minecraft:forest\".\n" +
                        "Examples: minecraft:plains, minecraft:desert, minecraft:forest, minecraft:swamp"))
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(value -> config.general_biomeWhitelist = new ArrayList<>(new LinkedHashSet<>(value)))
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Underwater Light Check"),
                        config.general_enableUnderwaterLightCheck)
                .setTooltip(Text.of("Enable light level checks when the player is underwater."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableUnderwaterLightCheck = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Affect Creative Players"),
                        config.general_affectCreativePlayers)
                .setTooltip(Text.of("Apply Murk's Grasp effect to players in Creative mode."))
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_affectCreativePlayers = value)
                .build());

        // Effect Category
        ConfigCategory effect = builder.getOrCreateCategory(Text.literal("Effect"));
        effect.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Effect Persistence Time (seconds)"),
                        config.effect_murksGraspPersistenceTime)
                .setTooltip(Text.of("Duration (in seconds) that effects persist after entering a lit area."))
                .setDefaultValue(4.0)
                .setMin(0.0)
                .setSaveConsumer(value -> config.effect_murksGraspPersistenceTime = value)
                .build());

        effect.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Blindness"),
                        config.effect_blindnessEnabled)
                .setTooltip(Text.of("Apply Blindness effect alongside Murk's Grasp."))
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.effect_blindnessEnabled = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Text.literal("Base Damage"),
                        config.effect_baseDamage)
                .setTooltip(Text.of("Initial damage per interval from Murk's Grasp (in half-hearts)."))
                .setDefaultValue(3.5f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_baseDamage = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Text.literal("Max Damage"),
                        config.effect_maxDamage)
                .setTooltip(Text.of("Maximum damage per interval after 60 seconds (in half-hearts)."))
                .setDefaultValue(6.0f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_maxDamage = value)
                .build());

        effect.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Damage Interval (seconds)"),
                        config.effect_damageInterval)
                .setTooltip(Text.of("Time between damage ticks from Murk's Grasp (in seconds)."))
                .setDefaultValue(4.5)
                .setMin(1.0)
                .setSaveConsumer(value -> config.effect_damageInterval = value)
                .build());

        // Light Source Category
        ConfigCategory lightSource = builder.getOrCreateCategory(Text.literal("Light Source"));
        lightSource.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Dropped Item Radius (blocks)"),
                        config.lightSource_droppedItemRadius)
                .setTooltip(Text.of("Radius (in blocks) to detect dropped light-emitting items."))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_droppedItemRadius = value)
                .build());

        lightSource.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Nearby Player Radius (blocks)"),
                        config.lightSource_nearbyPlayerRadius)
                .setTooltip(Text.of("Radius (in blocks) to detect light from nearby players' items."))
                .setDefaultValue(5.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_nearbyPlayerRadius = value)
                .build());

        return builder.build();
    }
}