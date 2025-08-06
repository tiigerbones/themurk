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

public class MurkConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_config");
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "murk.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static MurkConfig instance;

    public int lightThreshold = 4;
    public boolean enableWarningText = true;

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
                config = GSON.fromJson(reader, MurkConfig.class);
                LOGGER.info("Loaded config from {}", CONFIG_FILE.getPath());
            } catch (IOException e) {
                LOGGER.error("Failed to load config file", e);
            }
        } else {
            LOGGER.info("Config file not found, creating default at {}", CONFIG_FILE.getPath());
            config.saveConfig();
        }
        return config;
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            LOGGER.info("Saved config to {}", CONFIG_FILE.getPath());
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }
}