package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class GraspedPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayer player) {
        if (ConfigCache.isWarningTextEnabled() && !PlayerLightTracker.isGraspedNotified(player.getUUID())) {
            player.sendSystemMessage(
                    Component.literal("You are gripped by Murk’s Grasp!").withStyle(style -> style.withColor(0xFF5555)),
                    true
            );
            PlayerLightTracker.setGraspedNotified(player.getUUID(), true);
        }
        player.addEffect(new MobEffectInstance(
                Effects.MURKS_GRASP, -1, 0, false, true
        ));
        if (ConfigCache.isBlindnessEnabled()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS, -1, 0, false, false
            ));
        }
        PlayerLightTracker.reset(player.getUUID());
    }

    @Override
    public void onExit(ServerPlayer player) {
        PlayerLightTracker.clearGraspedNotified(player.getUUID());
    }

    @Override
    public void tick(ServerPlayer player, ServerLevel world) {
        PlayerLightTracker.incrementEffectTicks(player.getUUID());
    }
}