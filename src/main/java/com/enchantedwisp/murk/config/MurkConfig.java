package com.enchantedwisp.murk.config;

import com.enchantedwisp.murk.TheMurk;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MurkConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_config");
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "murk.json");
    private static final File README_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "murk_config_readme.txt");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static MurkConfig instance;

    public int lightThreshold = 3;
    public boolean enableWarningText = true;
    public double litAreaEffectDuration = 4.0; // Duration in seconds
    public List<String> dimensions = Arrays.asList("minecraft:overworld");

    public static MurkConfig getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    private static MurkConfig loadConfig() {
        MurkConfig config = new MurkConfig();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                MurkConfig loadedConfig = GSON.fromJson(reader, MurkConfig.class);
                if (isValidConfig(loadedConfig)) {
                    config = loadedConfig;
                    LOGGER.info("Loaded config from {}", CONFIG_FILE.getPath());
                } else {
                    LOGGER.warn("Config file is outdated or invalid, resetting to default values");
                    config.saveConfig();
                    saveReadme();
                }
            } catch (IOException | NullPointerException e) {
                LOGGER.error("Failed to load config file, resetting to default values", e);
                config.saveConfig();
                saveReadme();
            }
        } else {
            LOGGER.info("Config file not found, creating default at {}", CONFIG_FILE.getPath());
            config.saveConfig();
            saveReadme();
        }
        return config;
    }

    private static boolean isValidConfig(MurkConfig config) {
        if (config == null) {
            return false;
        }
        // Validate lightThreshold (0-15)
        if (config.lightThreshold < 0 || config.lightThreshold > 15) {
            LOGGER.warn("Invalid lightThreshold: {}. Must be between 0 and 15.", config.lightThreshold);
            return false;
        }
        // Validate enableWarningText (non-null)
        if (config.enableWarningText) {
            LOGGER.warn("Invalid enableWarningText: null. Must be true or false.");
            return false;
        }
        // Validate litAreaEffectDuration (positive number)
        if (config.litAreaEffectDuration <= 0 || Double.isNaN(config.litAreaEffectDuration)) {
            LOGGER.warn("Invalid litAreaEffectDuration: {}. Must be positive.", config.litAreaEffectDuration);
            return false;
        }
        // Validate dimensions (non-empty list)
        if (config.dimensions == null || config.dimensions.isEmpty()) {
            LOGGER.warn("Invalid dimensions: {}. Must be a non-empty list of dimension IDs.", config.dimensions);
            return false;
        }
        return true;
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            LOGGER.info("Saved config to {}", CONFIG_FILE.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }

    private static void saveReadme() {
        try (FileWriter writer = new FileWriter(README_FILE)) {
            writer.write(
                    """
                            Murk Mod Configuration
                            This file explains the options in murk.json.
                            
                            lightThreshold: Light level below which Murk's Grasp effect is triggered (0-15). Default: 3
                              - Set the light level threshold for applying the effect in dark areas.
                              - Example: 3 means the effect triggers in areas with light level below 3.
                            
                            enableWarningText: Whether to show warning and effect application messages in chat. Default: true
                              - Set to false to disable messages like 'Something lurks in the dark....' and 'You are gripped by Murk’s Grasp!'.
                            
                            litAreaEffectDuration: Duration (in seconds) that effects persist after entering a lit area. Default: 4.0
                              - Controls how long Murk's Grasp and Blindness effects last after moving to a lit area.
                              - Example: 2.0 means effects last 2 seconds.
                            
                            dimensions: List of dimension IDs where the effect applies (e.g., 'minecraft:overworld', 'minecraft:the_nether'). Default: ['minecraft:overworld']
                              - Specifies which dimensions the effect applies in.
                              - Example: ['minecraft:overworld', 'minecraft:the_nether'] enables the effect in both dimensions."""
            );
            LOGGER.info("Saved config readme to {}", README_FILE.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to save config readme file", e);
        }
    }
}