package com.enchantedwisp.murk.core;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface PhaseHandler {
    void onEnter(ServerPlayerEntity player);
    void onExit(ServerPlayerEntity player);
    void tick(ServerPlayerEntity player, ServerWorld world);
}