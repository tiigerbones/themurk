package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class GraspedPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayerEntity player) {
        if (ConfigCache.isWarningTextEnabled() && !PlayerLightTracker.isGraspedNotified(player.getUuid())) {
            player.sendMessageToClient(
                    Text.literal("You are gripped by Murk’s Grasp!").styled(style -> style.withColor(0xFF5555)),
                    true
            );
            PlayerLightTracker.setGraspedNotified(player.getUuid(), true);
        }
        player.addStatusEffect(new StatusEffectInstance(
                Effects.MURKS_GRASP, -1, 0, false, true
        ));
        if (ConfigCache.isBlindnessEnabled()) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, -1, 0, false, false
            ));
        }
        PlayerLightTracker.reset(player.getUuid());
    }

    @Override
    public void onExit(ServerPlayerEntity player) {
        PlayerLightTracker.clearGraspedNotified(player.getUuid());
    }

    @Override
    public void tick(ServerPlayerEntity player, ServerWorld world) {
        PlayerLightTracker.incrementEffectTicks(player.getUuid());
    }
}