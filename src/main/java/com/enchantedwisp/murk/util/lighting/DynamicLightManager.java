package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicLightManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");

        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(
                Identifier.of(TheMurk.MOD_ID, "dynamic_light_loader"),
                new LightSourceLoader()
        );
    }
}