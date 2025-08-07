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
                .setSavingRunnable(TheMurk::saveConfig); // Only save on explicit save

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        MurkConfig config = TheMurk.getConfig();

        // General Category
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(entryBuilder.startIntSlider(
                        Text.literal("Light Threshold"),
                        config.general_lightThreshold,
                        0, 15)
                .setDefaultValue(3)
                .setSaveConsumer(value -> config.general_lightThreshold = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Warning Text"),
                        config.general_enableWarningText)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.general_enableWarningText = value)
                .build());

        general.addEntry(entryBuilder.startStrList(
                        Text.literal("Dimensions"),
                        config.general_dimensions)
                .setDefaultValue(new ArrayList<>(Arrays.asList("minecraft:overworld")))
                .setSaveConsumer(value -> config.general_dimensions = new ArrayList<>(new LinkedHashSet<>(value))) // Deduplicate
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Underwater Light Check"),
                        config.general_enableUnderwaterLightCheck)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableUnderwaterLightCheck = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Enable Creative Effect"),
                        config.general_enableCreativeEffect)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.general_enableCreativeEffect = value)
                .build());

        // Effect Category
        ConfigCategory effect = builder.getOrCreateCategory(Text.literal("Effect"));
        effect.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Lit Area Effect Duration (seconds)"),
                        config.effect_litAreaEffectDuration)
                .setDefaultValue(4.0)
                .setMin(0.0)
                .setSaveConsumer(value -> config.effect_litAreaEffectDuration = value)
                .build());

        effect.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Blindness Enabled"),
                        config.effect_blindnessEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.effect_blindnessEnabled = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Text.literal("Base Damage"),
                        config.effect_baseDamage)
                .setDefaultValue(0.5f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_baseDamage = value)
                .build());

        effect.addEntry(entryBuilder.startFloatField(
                        Text.literal("Max Damage"),
                        config.effect_maxDamage)
                .setDefaultValue(2.0f)
                .setMin(0.0f)
                .setSaveConsumer(value -> config.effect_maxDamage = value)
                .build());

        effect.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Damage Interval (seconds)"),
                        config.effect_damageInterval)
                .setDefaultValue(3.0)
                .setMin(1.0)
                .setSaveConsumer(value -> config.effect_damageInterval = value)
                .build());

        // Light Source Category
        ConfigCategory lightSource = builder.getOrCreateCategory(Text.literal("Light Source"));
        lightSource.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Dropped Item Radius (blocks)"),
                        config.lightSource_droppedItemRadius)
                .setDefaultValue(7.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_droppedItemRadius = value)
                .build());

        lightSource.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Nearby Player Radius (blocks)"),
                        config.lightSource_nearbyPlayerRadius)
                .setDefaultValue(7.0)
                .setMin(1.0)
                .setMax(16.0)
                .setSaveConsumer(value -> config.lightSource_nearbyPlayerRadius = value)
                .build());

        return builder.build();
    }
}