package com.enchantedwisp.murk.util.tracker;

import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.lighting.LightSource;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;

public class LightLevelEvaluator {
    /**
     * Calculates the effective light level for a player.
     */
    public static int getEffectiveLightLevel(ServerPlayerEntity player) {
        int blockLight = player.getWorld().getLightLevel(player.getBlockPos());
        int itemLight = ConfigCache.isDynamicLightSupportEnabled()
                ? LightLevelEvaluator.getPlayerItemLightLevel(player)
                : 0;
        int droppedItemLight = ConfigCache.isDynamicLightSupportEnabled()
                ? getDroppedItemLightLevel(player)
                : 0;
        int nearbyPlayerLight = ConfigCache.isDynamicLightSupportEnabled()
                ? getNearbyPlayerLightLevel(player)
                : 0;
        return Math.max(blockLight, Math.max(itemLight, Math.max(droppedItemLight, nearbyPlayerLight)));
    }

    /**
     * Calculates the light level from a player's held items or trinkets.
     */
    public static int getPlayerItemLightLevel(PlayerEntity player) {
        if (!ConfigCache.isDynamicLightSupportEnabled()) {
            return 0;
        }

        if (!ConfigCache.isUnderwaterLightCheckEnabled() && player.isSubmergedInWater()) {
            return 0;
        }

        int maxLightLevel = 0;

        // Check main and off-hand
        for (ItemStack stack : new ItemStack[]{player.getMainHandStack(), player.getOffHandStack()}) {
            if (!stack.isEmpty()) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                LightSource entry = LightSource.getLightSources().get(itemId);
                if (entry != null) {
                    if (entry.waterSensitive && player.isSubmergedInWater()) {
                        continue;
                    }
                    maxLightLevel = Math.max(maxLightLevel, entry.luminance);
                }
            }
        }

        // Check trinkets
        if (isTrinketsLoaded()) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
            if (trinketComponent.isPresent()) {
                for (var group : trinketComponent.get().getAllEquipped()) {
                    ItemStack stack = group.getRight();
                    if (!stack.isEmpty()) {
                        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                        LightSource entry = LightSource.getLightSources().get(itemId);
                        if (entry != null) {
                            if (entry.waterSensitive && player.isSubmergedInWater()) {
                                continue;
                            }
                            maxLightLevel = Math.max(maxLightLevel, entry.luminance);
                        }
                    }
                }
            }
        }
        return maxLightLevel;
    }

    /**
     * Calculates the light level from dropped items within the configured radius.
     */
    private static int getDroppedItemLightLevel(ServerPlayerEntity player) {
        int maxLightLevel = 0;
        ServerWorld world = player.getServerWorld();
        double radius = ConfigCache.getDroppedItemRadius();
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
                LightSource entry = LightSource.getLightSources().get(itemId);
                if (entry != null) {
                    // Skip if water-sensitive and item is underwater
                    if (entry.waterSensitive && itemEntity.isSubmergedInWater()) {
                        continue;
                    }
                    // Check line of sight with transparency
                    Vec3d itemPos = itemEntity.getPos().add(0, 0.5, 0); // Center of item entity
                    if (RaycastUtil.hasLineOfSight(world, playerEyePos, itemPos)) {
                        continue;
                    }
                    if (entry.luminance > maxLightLevel) {
                        maxLightLevel = entry.luminance;
                    }
                }
            }
        }

        return maxLightLevel;
    }

    /**
     * Calculates the light level from nearby players within the configured radius.
     */
    private static int getNearbyPlayerLightLevel(ServerPlayerEntity player) {
        int maxLightLevel = 0;
        ServerWorld world = player.getServerWorld();
        double radius = ConfigCache.getNearbyPlayerRadius();
        Box box = new Box(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, box,
                nearbyPlayer -> nearbyPlayer != player && !nearbyPlayer.isSpectator());
        Vec3d playerEyePos = player.getEyePos();

        for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
            int lightLevel = getPlayerItemLightLevel(nearbyPlayer);
            if (lightLevel > 0) {
                // Check line of sight with transparency
                Vec3d nearbyPlayerEyePos = nearbyPlayer.getEyePos();
                if (RaycastUtil.hasLineOfSight(world, playerEyePos, nearbyPlayerEyePos)) {
                    continue;
                }
                if (lightLevel > maxLightLevel) {
                    maxLightLevel = lightLevel;
                }
            }
        }

        return maxLightLevel;
    }

    /**
     * Checks if Trinkets mod is loaded.
     */
    private static boolean isTrinketsLoaded() {
        try {
            Class.forName("dev.emi.trinkets.api.TrinketsApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}