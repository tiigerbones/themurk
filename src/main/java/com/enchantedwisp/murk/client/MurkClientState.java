package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MurkClientState {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client_state");
    private static boolean isWarningActive = false;

    public static boolean isWarningActive() {
        return isWarningActive;
    }

    public static void setWarningActive(boolean active) {
        if (isWarningActive != active) {
            LOGGER.info("Warning state changed to: {}", active);
            isWarningActive = active;
        }
    }
}