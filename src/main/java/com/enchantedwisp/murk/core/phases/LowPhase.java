package com.enchantedwisp.murk.core.phases;

import com.enchantedwisp.murk.core.PhaseHandler;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LowPhase implements PhaseHandler {

    @Override
    public void onEnter(ServerPlayer player) {
        PlayerLightTracker.reset(player.getUUID());
    }

    @Override
    public void onExit(ServerPlayer player) {}

    @Override
    public void tick(ServerPlayer player, ServerLevel world) {
        // Increment only in low light
        PlayerLightTracker.incrementLowLightTicks(player.getUUID());
    }
}