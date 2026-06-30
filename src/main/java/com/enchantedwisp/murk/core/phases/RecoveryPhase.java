package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class RecoveryPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayer player) {
        player.removeEffect(Effects.MURKS_GRASP);
        player.addEffect(new MobEffectInstance(
                Effects.MURKS_GRASP,
                ConfigCache.getLitAreaDurationTicks(),
                0,
                false,
                true
        ));
        if (ConfigCache.isBlindnessEnabled()) {
            player.removeEffect(MobEffects.BLINDNESS);
            player.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS,
                    ConfigCache.getLitAreaDurationTicks(),
                    0,
                    false,
                    false
            ));
        }

        PlayerLightTracker.setDurationReduced(player.getUUID(), true);
        PlayerLightTracker.resetEffectTicks(player.getUUID());
    }

    @Override
    public void onExit(ServerPlayer player) {
        PlayerLightTracker.clearDurationReduced(player.getUUID());
        PlayerLightTracker.clearEffectTicks(player.getUUID());
    }

    @Override
    public void tick(ServerPlayer player, ServerLevel world) {}
}
