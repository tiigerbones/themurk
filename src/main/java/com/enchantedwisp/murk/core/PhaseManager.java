package com.enchantedwisp.murk.core;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.network.DangerMusicNetworking;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.core.phases.*;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public static void updatePlayer(ServerPlayerEntity player, ServerWorld world, int lightLevel) {
        UUID id = player.getUuid();
        Phases current = playerPhases.getOrDefault(id, Phases.SAFE);
        Phases next = determineNextPhase(player, lightLevel, current);

        if (next != current) {
            handlers.get(current).onExit(player);
            handlers.get(next).onEnter(player);
            playerPhases.put(id, next);

            if (next == Phases.WARNING || next == Phases.GRASPED || next == Phases.RECOVERY) {
                DangerMusicNetworking.sendMusicSuppression(player, true);
            } else if (next == Phases.SAFE) {
                DangerMusicNetworking.sendMusicSuppression(player, false);
            }

            TheMurk.LOGGER.debug("Phase transition for {}: {} -> {} (light={}, hasGrasp={})",
                    player.getName().getString(), current, next, lightLevel, player.hasStatusEffect(Effects.MURKS_GRASP));
        }

        handlers.get(next).tick(player, world);
    }

    private static Phases determineNextPhase(ServerPlayerEntity player, int lightLevel, Phases current) {
        boolean hasGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);
        boolean isLit = lightLevel >= ConfigCache.getLightThreshold();
        UUID id = player.getUuid();

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