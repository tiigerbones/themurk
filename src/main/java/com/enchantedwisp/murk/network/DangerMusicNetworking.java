package com.enchantedwisp.murk.network;

import com.enchantedwisp.murk.TheMurk;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DangerMusicNetworking {

    /**
     * Unique packet identifier for the "suppress music" message.
     * Format: modid:packet_name
     */
    public static final Identifier DANGER_MUSIC_SUPPRESS =
            new Identifier(TheMurk.MOD_ID, "danger_music_suppress");

    private DangerMusicNetworking() {
        // Utility class - prevent instantiation
    }

    /**
     * Sends the music suppression state to one specific player.
     *
     * <p>Called from PhaseManager when a phase transition occurs that should
     * affect background music.
     *
     * @param player   the player who should receive the update
     * @param suppress true = stop/pause music (WARNING, GRASPED, RECOVERY),
     *                 false = allow normal music again (SAFE)
     */
    public static void sendMusicSuppression(ServerPlayerEntity player, boolean suppress) {
        // Create a new buffer and write the single boolean value
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(suppress);

        // Send the packet to this specific player only
        ServerPlayNetworking.send(player, DANGER_MUSIC_SUPPRESS, buf);

        TheMurk.LOGGER.debug("Sent music suppression={} to player {}",
                suppress, player.getName().getString());
    }

    /**
     * Registers any server-side packet handlers for this feature.
     * Currently empty because we only send from server → client.
     * Call this from TheMurk.onInitialize().
     */
    public static void register() {
        // No global server receiver needed for this one-way packet.
        // If we later want clients to request something, we can add it here.
        TheMurk.LOGGER.info("DangerMusicNetworking registered (server side ready)");
    }
}