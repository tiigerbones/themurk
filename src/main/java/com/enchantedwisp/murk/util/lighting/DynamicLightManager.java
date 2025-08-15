package com.enchantedwisp.murk.util.lighting;

import com.enchantedwisp.murk.TheMurk;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicLightManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_dynamic_lighting");
    private static boolean isDynamicLightingModLoaded = false;

    public static void register() {
        LOGGER.info("Initializing dynamic lighting support for The Murk");

        // Only initialize dynamic lighting on the client
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            isDynamicLightingModLoaded = isLambDynamicLightsLoaded() || isSodiumDynamicLightsLoaded();
            LOGGER.info("Dynamic lighting mod detected: {}", isDynamicLightingModLoaded ? "Yes" : "No");

            if (isDynamicLightingModLoaded) {
                ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                        new LightSourceLoader()
                );
            }
        } else {
            LOGGER.info("Running on server, skipping dynamic lighting initialization.");
        }
    }

    /**
     * Checks if a dynamic lighting mod is loaded.
     */
    public static boolean isDynamicLightingModLoaded() {
        return isDynamicLightingModLoaded;
    }

    /**
     * Checks if LambDynamicLights mod is loaded.
     */
    private static boolean isLambDynamicLightsLoaded() {
        try {
            Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if SodiumDynamicLights mod is loaded.
     */
    private static boolean isSodiumDynamicLightsLoaded() {
        try {
            Class.forName("toni.sodiumdynamiclights.SodiumDynamicLights");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}