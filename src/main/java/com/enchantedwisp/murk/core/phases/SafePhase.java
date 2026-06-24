package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

public class SafePhase implements PhaseHandler {
    @Override
    public void onEnter(ServerPlayer player) {
        PlayerLightTracker.reset(player.getUUID());
        player.removeEffect(Effects.MURKS_GRASP);
        player.removeEffect(MobEffects.BLINDNESS);
    }

    @Override
    public void onExit(ServerPlayer player) {}

    @Override
    public void tick(ServerPlayer player, ServerLevel world) {}
}
