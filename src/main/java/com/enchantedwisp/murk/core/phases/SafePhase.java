package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SafePhase implements PhaseHandler {
    @Override
    public void onEnter(ServerPlayerEntity player) {
        PlayerLightTracker.reset(player.getUuid());
        player.removeStatusEffect(Effects.MURKS_GRASP);
        player.removeStatusEffect(StatusEffects.BLINDNESS);
    }

    @Override
    public void onExit(ServerPlayerEntity player) {}

    @Override
    public void tick(ServerPlayerEntity player, ServerWorld world) {}
}
