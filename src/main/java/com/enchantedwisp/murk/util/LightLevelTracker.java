package com.enchantedwisp.murk.util;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.registry.Effects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LightLevelTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheMurk.MOD_ID + "_light_tracker");
    private static final int TICKS_UNTIL_WARNING = 100; // 5 seconds
    private static final int TICKS_AFTER_WARNING = 200; // 10 seconds
    private static final Map<UUID, Integer> playerLowLightTicks = new HashMap<>();
    private static final Map<UUID, Boolean> playerWarned = new HashMap<>();
    private static final Map<UUID, Boolean> playerDurationReduced = new HashMap<>();
    private static final Map<UUID, Integer> playerEffectTicks = new HashMap<>();

    public static void register() {
        LOGGER.info("Registering LightLevelTracker");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MurkConfig config = TheMurk.getConfig();
            int lightThreshold = config.general_lightThreshold;
            int litAreaDurationTicks = (int) (config.effect_litAreaEffectDuration * 20); // Convert seconds to ticks
            boolean enableCreativeEffect = config.general_enableCreativeEffect;
            boolean enableUnderwaterLightCheck = config.general_enableUnderwaterLightCheck;
            boolean enableWarningText = config.general_enableWarningText;
            boolean blindnessEnabled = config.effect_blindnessEnabled;
            List<String> allowedDimensions = config.general_dimensions;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getBlockPos();

                // Skip Creative mode players unless enabled
                if (player.isCreative() && !enableCreativeEffect) {
                    resetPlayer(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                        LOGGER.debug("Removed MurksGraspEffect for Creative player {}", player.getName().getString());
                    }
                    continue;
                }

                // Check if player is in an allowed dimension
                Identifier dimensionId = world.getRegistryKey().getValue();
                if (!allowedDimensions.contains(dimensionId.toString())) {
                    resetPlayer(playerId);
                    continue;
                }

                // Skip light checks if underwater and not enabled
                if (!enableUnderwaterLightCheck && player.isSubmergedInWater()) {
                    resetPlayer(playerId);
                    if (player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        if (blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                        }
                        LOGGER.debug("Removed MurksGraspEffect for player {} underwater", player.getName().getString());
                    }
                    continue;
                }

                // Check total light level (block + item + dropped items + nearby players)
                int lightLevel = getEffectiveLightLevel(player, config);
                LOGGER.debug("Player {} at {} in dimension {} has light level {}",
                        player.getName().getString(), pos, dimensionId, lightLevel);

                if (lightLevel < lightThreshold) {
                    // Player is in low light
                    if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                        // Increment tick counter if effect not yet applied
                        int ticks = playerLowLightTicks.getOrDefault(playerId, 0) + 1;
                        playerLowLightTicks.put(playerId, ticks);

                        // Send warning message after 5 seconds if enabled
                        if (enableWarningText && ticks >= TICKS_UNTIL_WARNING && !playerWarned.getOrDefault(playerId, false)) {
                            player.sendMessage(
                                    Text.literal("An evil presence lurks in the dark nearby...").styled(style -> style.withColor(0xFF5555)),
                                    false
                            );
                            playerWarned.put(playerId, true);
                            LOGGER.debug("Sent warning message to player {}", player.getName().getString());
                        }

                        // Apply effect with infinite duration after 15 seconds (5 + 10)
                        if (ticks >= TICKS_UNTIL_WARNING + TICKS_AFTER_WARNING) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    Effects.MURKS_GRASP,
                                    -1, // Infinite duration
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    true // Show particles
                            ));
                            if (blindnessEnabled) {
                                player.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.BLINDNESS,
                                        -1, // Infinite duration
                                        0, // Amplifier 0
                                        false, // Not ambient
                                        false // Hide particles
                                ));
                            }
                            LOGGER.info("Applied MurksGraspEffect (infinite) to player {}", player.getName().getString());
                            resetPlayer(playerId); // Reset timer after applying effect
                            playerEffectTicks.put(playerId, 0); // Initialize effect timer
                        }
                    } else {
                        // Increment effect duration
                        playerEffectTicks.put(playerId, playerEffectTicks.getOrDefault(playerId, 0) + 1);
                    }
                    // Reset duration reduction flag when entering low light
                    playerDurationReduced.remove(playerId);
                } else {
                    // Player is in lit area (light level >= threshold)
                    if (player.hasStatusEffect(Effects.MURKS_GRASP) && !playerDurationReduced.getOrDefault(playerId, false)) {
                        // Remove and reapply MurksGraspEffect with configured duration
                        player.removeStatusEffect(Effects.MURKS_GRASP);
                        player.addStatusEffect(new StatusEffectInstance(
                                Effects.MURKS_GRASP,
                                litAreaDurationTicks,
                                0, // Amplifier 0
                                false, // Not ambient
                                true // Show particles
                        ));
                        // Remove and reapply Blindness effect with configured duration if enabled
                        if (blindnessEnabled) {
                            player.removeStatusEffect(StatusEffects.BLINDNESS);
                            player.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.BLINDNESS,
                                    litAreaDurationTicks,
                                    0, // Amplifier 0
                                    false, // Not ambient
                                    false // Hide particles
                            ));
                        }
                        playerDurationReduced.put(playerId, true);
                        playerEffectTicks.put(playerId, 0); // Reset effect ticks when entering lit area
                        LOGGER.debug("Reduced MurksGraspEffect and {} duration to {} ticks for player {}",
                                blindnessEnabled ? "Blindness" : "no Blindness", litAreaDurationTicks, player.getName().getString());
                    }
                    // Reset timer and warning state
                    resetPlayer(playerId);
                }

                // Clear duration reduction flag and effect ticks if effect expires
                if (!player.hasStatusEffect(Effects.MURKS_GRASP)) {
                    playerDurationReduced.remove(playerId);
                    playerEffectTicks.remove(playerId);
                }
            }
        });
    }

    private static int getEffectiveLightLevel(ServerPlayerEntity player, MurkConfig config) {
        int blockLight = player.getWorld().getLightLevel(player.getBlockPos());
        int itemLight = DynamicLightingHandler.isDynamicLightingModLoaded()
                ? DynamicLightingHandler.getPlayerLightLevel(player)
                : 0;
        int droppedItemLight = DynamicLightingHandler.isDynamicLightingModLoaded()
                ? getDroppedItemLightLevel(player, config)
                : 0;
        int nearbyPlayerLight = DynamicLightingHandler.isDynamicLightingModLoaded()
                ? getNearbyPlayerLightLevel(player, config)
                : 0;
        int totalLight = Math.max(blockLight, Math.max(itemLight, Math.max(droppedItemLight, nearbyPlayerLight)));

        LOGGER.debug("Player {}: Block light {}, Item light {}, Dropped item light {}, Nearby player light {}, Total light {}",
                player.getName().getString(), blockLight, itemLight, droppedItemLight, nearbyPlayerLight, totalLight);
        return totalLight;
    }

    private static int getDroppedItemLightLevel(ServerPlayerEntity player, MurkConfig config) {
        int maxLightLevel = 0;
        ServerWorld world = player.getServerWorld();
        double radius = config.lightSource_droppedItemRadius;
        Box box = new Box(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ItemEntity> droppedItems = world.getEntitiesByClass(ItemEntity.class, box, entity -> true);
        Vec3d playerEyePos = player.getEyePos();

        for (ItemEntity itemEntity : droppedItems) {
            ItemStack stack = itemEntity.getStack();
            if (!stack.isEmpty()) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                DynamicLightingHandler.LightSourceEntry entry = DynamicLightingHandler.getLightSources().get(itemId);
                if (entry != null) {
                    // Skip if water-sensitive and item is underwater
                    if (entry.waterSensitive && itemEntity.isSubmergedInWater()) {
                        LOGGER.debug("Skipping dropped item {} at {}: Water-sensitive and submerged",
                                itemId, itemEntity.getBlockPos());
                        continue;
                    }
                    // Check line of sight with transparency
                    Vec3d itemPos = itemEntity.getPos().add(0, 0.5, 0); // Center of item entity
                    if (!hasLineOfSight(world, playerEyePos, itemPos)) {
                        LOGGER.debug("Skipping dropped item {} at {}: No transparent line of sight to player {}",
                                itemId, itemEntity.getBlockPos(), player.getName().getString());
                        continue;
                    }
                    if (entry.luminance > maxLightLevel) {
                        maxLightLevel = entry.luminance;
                        LOGGER.debug("Found dropped item {} at {} with luminance {} for player {}",
                                itemId, itemEntity.getBlockPos(), entry.luminance, player.getName().getString());
                    }
                }
            }
        }

        return maxLightLevel;
    }

    private static int getNearbyPlayerLightLevel(ServerPlayerEntity player, MurkConfig config) {
        int maxLightLevel = 0;
        ServerWorld world = player.getServerWorld();
        double radius = config.lightSource_nearbyPlayerRadius;
        Box box = new Box(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, box,
                nearbyPlayer -> nearbyPlayer != player && !nearbyPlayer.isSpectator());
        Vec3d playerEyePos = player.getEyePos();

        for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
            int lightLevel = DynamicLightingHandler.getPlayerLightLevel(nearbyPlayer);
            if (lightLevel > 0) {
                // Check line of sight with transparency
                Vec3d nearbyPlayerEyePos = nearbyPlayer.getEyePos();
                if (!hasLineOfSight(world, playerEyePos, nearbyPlayerEyePos)) {
                    LOGGER.debug("Skipping nearby player {} at {}: No transparent line of sight to player {}",
                            nearbyPlayer.getName().getString(), nearbyPlayer.getBlockPos(), player.getName().getString());
                    continue;
                }
                if (lightLevel > maxLightLevel) {
                    maxLightLevel = lightLevel;
                    LOGGER.debug("Found nearby player {} at {} with light level {} for player {}",
                            nearbyPlayer.getName().getString(), nearbyPlayer.getBlockPos(), lightLevel, player.getName().getString());
                }
            }
        }

        return maxLightLevel;
    }

    private static boolean hasLineOfSight(ServerWorld world, Vec3d start, Vec3d end) {
        RaycastContext context = new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.VISUAL, // Use VISUAL to respect transparent blocks like glass
                RaycastContext.FluidHandling.NONE,
                null
        );

        BlockHitResult hitResult = world.raycast(context);
        if (hitResult.getType() == BlockHitResult.Type.MISS) {
            LOGGER.debug("Line of sight clear from {} to {}", start, end);
            return true; // No blocks hit, clear line of sight
        }

        // Check if all blocks in the path are transparent
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        double stepSize = 0.1; // Small step size to check blocks along the path
        for (double d = 0; d <= distance; d += stepSize) {
            Vec3d point = start.add(direction.multiply(d));
            BlockPos pos = BlockPos.ofFloored(point);
            BlockState state = world.getBlockState(pos);
            if (!state.isTransparent(world, pos) && !state.isAir()) {
                LOGGER.debug("Line of sight blocked by non-transparent block {} at {}", state.getBlock().getName().getString(), pos);
                return false;
            }
        }

        LOGGER.debug("Line of sight clear through transparent blocks from {} to {}", start, end);
        return true; // All blocks are transparent or air
    }

    private static void resetPlayer(UUID playerId) {
        playerLowLightTicks.remove(playerId);
        playerWarned.remove(playerId);
        playerEffectTicks.remove(playerId);
        LOGGER.debug("Reset light tracking for player UUID {}", playerId);
    }
}