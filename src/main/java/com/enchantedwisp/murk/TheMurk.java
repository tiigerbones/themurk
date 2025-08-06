package com.enchantedwisp.murk;

import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import com.enchantedwisp.murk.util.LightLevelTracker;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheMurk implements ModInitializer {
	public static final String MOD_ID = "murk";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Murk mod");

		try {
			// Register the config with Auto Config using Jankson for JSON5
			AutoConfig.register(MurkConfig.class, JanksonConfigSerializer::new);
			LOGGER.info("Successfully registered MurkConfig with AutoConfig");
		} catch (Exception e) {
			LOGGER.error("Failed to register MurkConfig with AutoConfig", e);
		}

		// Register effects
		Effects.register();

		// Register sounds
		Sounds.register();

		// Register light level tracker
		LightLevelTracker.register();
	}
}