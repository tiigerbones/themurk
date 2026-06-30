package com.enchantedwisp.murk.core;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.core.phases.*;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class PhaseManager {
    private static final Map<UUID, Phases> playerPhases = new HashMap<>();
    private static final EnumMap<Phases, PhaseHandler> handlers = new EnumMap<>(Phases.class);

    static {
        handlers.put(Phases.SAFE, new SafePhase());
        handlers.put(Phases.LOW_LIGHT, new LowPhase());
        handlers.put(Phases.WARNING, new WarningPhase());
        handlers.put(Phases.GRASPED, new GraspedPhase());
        handlers.put(Phases.RECOVERY, new RecoveryPhase());
    }

    public static void updatePlayer(ServerPlayer player, ServerLevel world, int lightLevel) {
        UUID id = player.getUUID();
        Phases current = playerPhases.getOrDefault(id, Phases.SAFE);
        Phases next = determineNextPhase(player, lightLevel, current);

        if (next != current) {
            handlers.get(current).onExit(player);
            handlers.get(next).onEnter(player);
            playerPhases.put(id, next);
            TheMurk.LOGGER.debug("Phase transition for {}: {} -> {} (light={}, hasGrasp={})",
                    player.getName().getString(), current, next, lightLevel, player.hasEffect(Effects.MURKS_GRASP));
        }

        handlers.get(next).tick(player, world);
    }

    private static Phases determineNextPhase(ServerPlayer player, int lightLevel, Phases current) {
        boolean hasGrasp = player.hasEffect(Effects.MURKS_GRASP);
        boolean isLit = lightLevel >= ConfigCache.getLightThreshold();
        UUID id = player.getUUID();

        if (hasGrasp) {
            if (isLit) {
                // Lit + has: Enter/stay RECOVERY (finite linger) if not already reduced
                if (!PlayerLightTracker.isDurationReduced(id)) {
                    PlayerLightTracker.setDurationReduced(id, true);
                    PlayerLightTracker.resetEffectTicks(id);
                }
                return Phases.RECOVERY;
            } else {
                // Low + has: Go to GRASPED (keep finite, skip buildup/damage via flag)
                return Phases.GRASPED;
            }
        } else {
            // No effect
            if (isLit) {
                return Phases.SAFE;
            } else {
                // Low + no effect: Progress buildup
                switch (current) {
                    case SAFE, RECOVERY -> {
                        return Phases.LOW_LIGHT;
                    }
                    case LOW_LIGHT -> {
                        int ticks = PlayerLightTracker.getLowLightTicks(id);
                        if (ticks >= ConfigCache.getTicksUntilWarning()) return Phases.WARNING;
                    }
                    case WARNING -> {
                        int ticks = PlayerLightTracker.getLowLightTicks(id);
                        if (ticks >= ConfigCache.getTicksUntilWarning() + ConfigCache.getTicksAfterWarning())
                            return Phases.GRASPED;
                    }
                    case GRASPED -> {
                        // Expiry in low: Restart buildup
                        return Phases.LOW_LIGHT;
                    }
                    default -> {}
                }
                return current;
            }
        }
    }

    public static void resetPhase(UUID id) {
        playerPhases.remove(id);
    }
}