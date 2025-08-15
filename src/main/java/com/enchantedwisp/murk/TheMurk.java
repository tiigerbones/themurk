package com.enchantedwisp.murk;

import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.network.MurkNetworking;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Particles;
import com.enchantedwisp.murk.registry.Sounds;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.lighting.DynamicLightManager;
import com.enchantedwisp.murk.util.tracker.LightLevelMonitor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheMurk implements ModInitializer {
	public static final String MOD_ID = "murk";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static ConfigHolder<MurkConfig> configHolder;
	private static MurkConfig config;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Murk");

		// Register config
		try {
			configHolder = AutoConfig.register(MurkConfig.class, JanksonConfigSerializer::new);
			config = configHolder.getConfig();
		} catch (Exception e) {
			LOGGER.error("Failed to deserialize MurkConfig from config/murk.json5, falling back to defaults", e);
			config = new MurkConfig();
			configHolder = AutoConfig.getConfigHolder(MurkConfig.class);
		}

		// Initialize config cache
		ConfigCache.initialize();

        // Register effects
        Effects.register();

        // Register sounds
        Sounds.init();

        // Initialize dynamic lighting
        DynamicLightManager.register();

		// Register light level monitor
		LightLevelMonitor.register();

        // Server-side networking registration
        MurkNetworking.registerServerReceivers();

        // Register Particles
        Particles.register();
	}

	public static MurkConfig getConfig() {
		return config;
	}

	public static void saveConfig() {
		try {
			configHolder.save();
		} catch (Exception e) {
			LOGGER.error("Failed to save MurkConfig to config/murk.json5", e);
		}
	}
}