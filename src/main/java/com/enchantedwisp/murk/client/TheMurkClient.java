package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.shader.ChromaticShaderManager;
import com.enchantedwisp.murk.client.sound.MurkSoundManager;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TheMurkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client");
    private static final Map<UUID, Boolean> hadMurkGraspLastTick = new HashMap<>();
    private static int lastFramebufferWidth = 0;
    private static int lastFramebufferHeight = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing The Murk client");

        // Initialize shader when client is fully started
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ChromaticShaderManager.init();
            // Store initial framebuffer dimensions
            lastFramebufferWidth = client.getWindow().getFramebufferWidth();
            lastFramebufferHeight = client.getWindow().getFramebufferHeight();
            LOGGER.info("Chromatic shader initialized on client start");
        });

        // Register HUD render callback to detect framebuffer size changes
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int currentWidth = client.getWindow().getFramebufferWidth();
            int currentHeight = client.getWindow().getFramebufferHeight();

            // Check if framebuffer size has changed
            if (currentWidth != lastFramebufferWidth || currentHeight != lastFramebufferHeight) {
                ChromaticShaderManager.onWindowResize(currentWidth, currentHeight);
                LOGGER.debug("Framebuffer resized to {}x{}", currentWidth, currentHeight);
                lastFramebufferWidth = currentWidth;
                lastFramebufferHeight = currentHeight;
            }
        });

        // Register client tick event for tick-based logic only (sounds, vanish sound, effect tracking)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ScreenEffectManager.tick();
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                UUID playerId = player.getUuid();
                boolean hasMurkGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);

                // Manage sound effects
                if (hasMurkGrasp) {
                    MurkSoundManager.startSound(player);
                } else {
                    MurkSoundManager.stopSound(player);
                }

                // Play murk_vanish sound when MurksGraspEffect expires
                boolean hadEffectLastTick = hadMurkGraspLastTick.getOrDefault(playerId, false);
                if (hadEffectLastTick && !hasMurkGrasp) {
                    MinecraftClient.getInstance().getSoundManager().play(
                            new PositionedSoundInstance(
                                    Sounds.MURK_VANISH,
                                    SoundCategory.PLAYERS,
                                    1.0f, // Volume
                                    1.0f, // Pitch
                                    SoundInstance.createRandom(),
                                    player.getX(),
                                    player.getY(),
                                    player.getZ()
                            )
                    );
                }

                // Update effect tracking
                hadMurkGraspLastTick.put(playerId, hasMurkGrasp);
            }
        });

        // NEW: Register world render last event for frame-based shader rendering
        WorldRenderEvents.LAST.register(context -> {
            float tickDelta = context.tickDelta();  // Accurate partial tick for smooth animation
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            boolean hasMurkGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);
            ChromaticShaderManager.setIntensity(hasMurkGrasp ? 1f : 0f);
            ChromaticShaderManager.render(tickDelta, hasMurkGrasp);  // This applies the post-effect every frame
        });
    }
}