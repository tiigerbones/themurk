package com.enchantedwisp.murk;

import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheMurk implements ModInitializer {
	public static final String MOD_ID = "murk";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Murk");
		Effects.register();
		Sounds.register();
	}
}