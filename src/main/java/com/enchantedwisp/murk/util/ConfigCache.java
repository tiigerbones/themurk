package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import me.shedaniel.autoconfig.AutoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Caches MurkConfig values for performance and updates them on config save.
 */
public class ConfigCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_config_cache");
    private static MurkConfig config;
    private static int lightThreshold;
    private static int litAreaDurationTicks;
    private static int ticksUntilWarning;
    private static int ticksAfterWarning;
    private static boolean enableCreativeEffect;
    private static boolean enableUnderwaterLightCheck;
    private static boolean enableWarningText;
    private static boolean blindnessEnabled;
    private static boolean useBiomeWhitelist;
    private static List<String> allowedDimensions;
    private static List<String> biomeBlacklist;
    private static List<String> biomeWhitelist;
    private static double droppedItemRadius;
    private static double nearbyPlayerRadius;

    /**
     * Initializes the config cache and registers a save listener.
     */
    public static void initialize() {
        updateConfig();
        AutoConfig.getConfigHolder(MurkConfig.class).registerSaveListener((holder, config) -> {
            LOGGER.info("Config saved, updating cache");
            updateConfig();
            return null;
        });
    }

    /**
     * Updates cached config values.
     */
    private static void updateConfig() {
        config = TheMurk.getConfig();
        lightThreshold = config.general_lightThreshold;
        litAreaDurationTicks = (int) (config.effect_murksGraspPersistenceTime * 20);
        ticksUntilWarning = (int) (config.general_warningMessageDelay * 20);
        ticksAfterWarning = (int) (config.general_effectDelayAfterWarning * 20);
        enableCreativeEffect = config.general_affectCreativePlayers;
        enableUnderwaterLightCheck = config.general_enableUnderwaterLightCheck;
        enableWarningText = config.general_enableWarningText;
        blindnessEnabled = config.effect_blindnessEnabled;
        useBiomeWhitelist = config.general_useBiomeWhitelist;
        allowedDimensions = config.general_dimensions;
        biomeBlacklist = config.general_biomeBlacklist;
        biomeWhitelist = config.general_biomeWhitelist;
        droppedItemRadius = config.lightSource_droppedItemRadius;
        nearbyPlayerRadius = config.lightSource_nearbyPlayerRadius;
    }

    public static int getLightThreshold() {
        return lightThreshold;
    }

    public static int getLitAreaDurationTicks() {
        return litAreaDurationTicks;
    }

    public static int getTicksUntilWarning() {
        return ticksUntilWarning;
    }

    public static int getTicksAfterWarning() {
        return ticksAfterWarning;
    }

    public static boolean isCreativeEffectEnabled() {
        return enableCreativeEffect;
    }

    public static boolean isUnderwaterLightCheckEnabled() {
        return enableUnderwaterLightCheck;
    }

    public static boolean isWarningTextEnabled() {
        return enableWarningText;
    }

    public static boolean isBlindnessEnabled() {
        return blindnessEnabled;
    }

    public static boolean useBiomeWhitelist() {
        return useBiomeWhitelist;
    }

    public static List<String> getAllowedDimensions() {
        return allowedDimensions;
    }

    public static List<String> getBiomeBlacklist() {
        return biomeBlacklist;
    }

    public static List<String> getBiomeWhitelist() {
        return biomeWhitelist;
    }

    public static double getDroppedItemRadius() {
        return droppedItemRadius;
    }

    public static double getNearbyPlayerRadius() {
        return nearbyPlayerRadius;
    }
}