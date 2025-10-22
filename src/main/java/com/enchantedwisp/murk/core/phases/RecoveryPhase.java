package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class RecoveryPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayerEntity player) {
        player.removeStatusEffect(Effects.MURKS_GRASP);
        player.addStatusEffect(new StatusEffectInstance(
                Effects.MURKS_GRASP,
                ConfigCache.getLitAreaDurationTicks(),
                0,
                false,
                true
        ));
        if (ConfigCache.isBlindnessEnabled()) {
            player.removeStatusEffect(StatusEffects.BLINDNESS);
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS,
                    ConfigCache.getLitAreaDurationTicks(),
                    0,
                    false,
                    false
            ));
        }

        PlayerLightTracker.setDurationReduced(player.getUuid(), true);
        PlayerLightTracker.resetEffectTicks(player.getUuid());
    }

    @Override
    public void onExit(ServerPlayerEntity player) {
        PlayerLightTracker.clearDurationReduced(player.getUuid());
        PlayerLightTracker.clearEffectTicks(player.getUuid());
    }

    @Override
    public void tick(ServerPlayerEntity player, ServerWorld world) {}
}
