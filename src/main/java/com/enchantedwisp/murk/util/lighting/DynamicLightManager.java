package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicLightManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");
        // Register the light source loader for server data (loads on server and client)
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new LightSourceLoader());
    }
}