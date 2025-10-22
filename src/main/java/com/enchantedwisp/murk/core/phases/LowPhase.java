package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class LowPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayerEntity player) {
        PlayerLightTracker.reset(player.getUuid());
    }

    @Override
    public void onExit(ServerPlayerEntity player) {}

    @Override
    public void tick(ServerPlayerEntity player, ServerWorld world) {
        // Increment only in low light
        PlayerLightTracker.incrementLowLightTicks(player.getUuid());
    }
}