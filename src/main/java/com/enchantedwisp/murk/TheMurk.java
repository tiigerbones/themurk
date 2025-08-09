package com.enchantedwisp.murk;

import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.DynamicLightingHandler;
import com.enchantedwisp.murk.util.LightLevelTracker;
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
			LOGGER.info("Loaded MurkConfig from config/murk.json5. Contents: dimensions={}, lightThreshold={}",
					config.general_dimensions, config.general_lightThreshold);
		} catch (Exception e) {
			LOGGER.error("Failed to deserialize MurkConfig from config/murk.json5, falling back to defaults", e);
			config = new MurkConfig();
			configHolder = AutoConfig.getConfigHolder(MurkConfig.class);
		}

		// Register effects
		Effects.register();

		// Register dynamic lighting handler
		DynamicLightingHandler.register();

		// Register light level tracker
		LightLevelTracker.register();

		// Log config after initialization
		LOGGER.info("Post-initialization config: dimensions={}, lightThreshold={}",
				config.general_dimensions, config.general_lightThreshold);
	}

	public static MurkConfig getConfig() {
		return config;
	}

	public static void saveConfig() {
		try {
			configHolder.save();
			LOGGER.info("Manually saved MurkConfig to config/murk.json5. Contents: dimensions={}, lightThreshold={}",
					config.general_dimensions, config.general_lightThreshold);
		} catch (Exception e) {
			LOGGER.error("Failed to save MurkConfig to config/murk.json5", e);
		}
	}
}