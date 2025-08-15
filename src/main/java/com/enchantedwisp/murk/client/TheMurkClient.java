package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.shader.ChromaticAberrationPostProcessor;
import com.enchantedwisp.murk.client.sound.MurkSoundManager;
import com.enchantedwisp.murk.network.MurkNetworking;
import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Particles;
import com.enchantedwisp.murk.registry.Sounds;
import com.enchantedwisp.murk.util.ConfigCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TheMurkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_client");
    private static final Map<UUID, Boolean> hadMurkGraspLastTick = new HashMap<>();
    private static int particleSpawnCooldown = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing The Murk client");

        // Networking (client side packet listeners)
        MurkNetworking.registerClientReceivers();
        // Register particle factories
        Particles.registerClient();

        PostProcessHandler.addInstance(ChromaticAberrationPostProcessor.INSTANCE);

        // Register client tick event to toggle effects, sounds, and play murk_vanish when effect expires
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                UUID playerId = player.getUuid();
                boolean hasMurkGrasp = player.hasStatusEffect(Effects.MURKS_GRASP);

                // Toggle ChromaticAberrationPostProcessor
                if (hasMurkGrasp && !ChromaticAberrationPostProcessor.INSTANCE.isActive()) {
                    ChromaticAberrationPostProcessor.INSTANCE.setActive(true);

                } else if (!hasMurkGrasp && ChromaticAberrationPostProcessor.INSTANCE.isActive()) {
                    ChromaticAberrationPostProcessor.INSTANCE.setActive(false);

                }

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

                // Spawn blinking eyes particle during warning period
                if (MurkClientState.isWarningActive() && ConfigCache.isEyesParticleEnabled() && !hasMurkGrasp) {
                    if (particleSpawnCooldown <= 0) {
                        // Calculate spawn position behind player
                        float yaw = MathHelper.wrapDegrees(player.getYaw());
                        float pitch = MathHelper.clamp(player.getPitch(), -90.0f, 90.0f);
                        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw).normalize();
                        double distance = 2.0 + player.getRandom().nextDouble() * 8.0; // 2-10 blocks
                        Vec3d spawnPos = player.getPos().add(lookVec.multiply(-distance)); // Behind player
                        spawnPos = new Vec3d(spawnPos.x, player.getEyeY() + (player.getRandom().nextDouble() - 0.5) * 2.0, spawnPos.z);

                        // Adjust to nearest air block
                        BlockPos pos = BlockPos.ofFloored(spawnPos);
                        int attempts = 0;
                        while (attempts < 10) {
                            assert client.world != null;
                            if (!(client.world.getBlockState(pos).getBlock() != Blocks.AIR || client.world.getLightLevel(LightType.BLOCK, pos) >= ConfigCache.getLightThreshold()))
                                break;
                            spawnPos = spawnPos.add(
                                    (player.getRandom().nextDouble() - 0.5) * 2.0,
                                    (player.getRandom().nextDouble() - 0.5) * 2.0,
                                    (player.getRandom().nextDouble() - 0.5) * 2.0
                            );
                            pos = BlockPos.ofFloored(spawnPos);
                            attempts++;
                        }

                        // Spawn particle if valid position found
                        if (client.world.getBlockState(pos).getBlock() == Blocks.AIR && client.world.getLightLevel(LightType.BLOCK, pos) < ConfigCache.getLightThreshold()) {
                            client.world.addParticle(
                                    (ParticleEffect) Particles.BLINKING_EYES,
                                    spawnPos.x, spawnPos.y, spawnPos.z,
                                    0.0, 0.0, 0.0
                            );
                            particleSpawnCooldown = 20; // Prevent spawning multiple particles
                        }
                    } else {
                        particleSpawnCooldown--;
                    }
                } else {
                    particleSpawnCooldown = 0; // Reset cooldown when not in warning period
                }
            }
        });
    }
}