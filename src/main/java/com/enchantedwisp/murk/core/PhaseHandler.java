package com.enchantedwisp.murk.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface PhaseHandler {
    void onEnter(ServerPlayer player);
    void onExit(ServerPlayer player);
    void tick(ServerPlayer player, ServerLevel world);
}