package com.enchantedwisp.murk.network;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.MurkClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

public class MurkNetworking {
    public static final Identifier START_WARNING_PACKET_ID = new Identifier(TheMurk.MOD_ID, "start_warning");
    public static final Identifier STOP_WARNING_PACKET_ID = new Identifier(TheMurk.MOD_ID, "stop_warning");

    // SERVER → CLIENT
    public static void sendStartWarningPacket(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, START_WARNING_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void sendStopWarningPacket(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, STOP_WARNING_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
    }

    // CLIENT packet receivers
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(START_WARNING_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> MurkClientState.setWarningActive(true));
        });

        ClientPlayNetworking.registerGlobalReceiver(STOP_WARNING_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> MurkClientState.setWarningActive(false));
        });
    }

    // SERVER packet receivers (optional for client → server communication)
    public static void registerServerReceivers() {
    }
}
