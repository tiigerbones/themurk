package com.enchantedwisp.murk.util.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLightTracker {
    private static final Map<UUID, Integer> playerLowLightTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerWarned = new HashMap<>();
    private static final Map<UUID, Boolean> playerDurationReduced = new HashMap<>();
    private static final Map<UUID, Integer> playerEffectTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerGraspedNotified = new HashMap<>();

    /**
     * Increments the low light tick counter for a player.
     */
    public static void incrementLowLightTicks(UUID playerId) {
        playerLowLightTicks.put(playerId, playerLowLightTicks.getOrDefault(playerId, 0) + 1);
    }

    /**
     * Gets the low light tick count for a player.
     */
    public static int getLowLightTicks(UUID playerId) {
        return playerLowLightTicks.getOrDefault(playerId, 0);
    }

    /**
     * Sets the warned state for a player.
     */
    public static void setWarned(UUID playerId, boolean warned) {
        playerWarned.put(playerId, warned);
    }

    /**
     * Checks if a player has been warned.
     */
    public static boolean isWarned(UUID playerId) {
        return playerWarned.getOrDefault(playerId, false);
    }

    /**
     * Sets the duration reduced flag for a player.
     */
    public static void setDurationReduced(UUID playerId, boolean reduced) {
        playerDurationReduced.put(playerId, reduced);
    }

    /**
     * Checks if a player's effect duration has been reduced.
     */
    public static boolean isDurationReduced(UUID playerId) {
        return playerDurationReduced.getOrDefault(playerId, false);
    }

    /**
     * Increments the effect tick counter for damage ramping.
     */
    public static void incrementEffectTicks(UUID playerId) {
        playerEffectTicks.put(playerId, playerEffectTicks.getOrDefault(playerId, 0) + 1);
    }

    /**
     * Gets the effect tick count for a player.
     */
    public static int getEffectTicks(UUID playerId) {
        return playerEffectTicks.getOrDefault(playerId, 0);
    }

    /**
     * Resets the effect tick counter for a player.
     */
    public static void resetEffectTicks(UUID playerId) {
        playerEffectTicks.put(playerId, 0);
    }

    /**
     * Clears the duration reduced flag for a player.
     */
    public static void clearDurationReduced(UUID playerId) {
        playerDurationReduced.remove(playerId);
    }

    /**
     * Clears the effect tick counter for a player.
     */
    public static void clearEffectTicks(UUID playerId) {
        playerEffectTicks.remove(playerId);
    }

    // New: Grasp notification flag methods (like warned)
    /**
     * Sets the grasped notified state for a player.
     */
    public static void setGraspedNotified(UUID playerId, boolean notified) {
        playerGraspedNotified.put(playerId, notified);
    }

    /**
     * Checks if a player has been notified of grasp.
     */
    public static boolean isGraspedNotified(UUID playerId) {
        return playerGraspedNotified.getOrDefault(playerId, false);
    }

    /**
     * Clears the grasped notified flag for a player.
     */
    public static void clearGraspedNotified(UUID playerId) {
        playerGraspedNotified.remove(playerId);
    }

    /**
     * Resets all state for a player.
     */
    public static void reset(UUID playerId) {
        playerLowLightTicks.remove(playerId);
        playerWarned.remove(playerId);
        playerDurationReduced.remove(playerId);
        playerEffectTicks.remove(playerId);
        playerGraspedNotified.remove(playerId);
    }
}