package com.enchantedwisp.murk.util.tracker;

import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.lighting.LightSource;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LightLevelEvaluator {
    /**
     * Checks if Trinkets mod is loaded.
     */
    private static final boolean TRINKETS_LOADED;
    static {
        boolean loaded;
        try {
            Class.forName("eu.pb4.trinkets.api.TrinketsApi");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
        TRINKETS_LOADED = loaded;
    }

    /**
     * Calculates the effective light level for a player.
     */
    public static int getEffectiveLightLevel(ServerPlayer player) {
        int blockLight = player.level().getMaxLocalRawBrightness(player.blockPosition());
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
    public static int getPlayerItemLightLevel(Player player) {
        if (!ConfigCache.isDynamicLightSupportEnabled()) {
            return 0;
        }

        if (!ConfigCache.isUnderwaterLightCheckEnabled() && player.isUnderWater()) {
            return 0;
        }

        int maxLightLevel = 0;

        // Check main and off-hand
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                LightSource entry = LightSource.getLightSources().get(itemId);
                if (entry != null) {
                    if (entry.waterSensitive && player.isUnderWater()) {
                        continue;
                    }
                    maxLightLevel = Math.max(maxLightLevel, entry.luminance);
                }
            }
        }

        // Check trinkets
        if (TRINKETS_LOADED) {
            var attachment = TrinketsApi.getAttachment(player);

            if (attachment != null) {
                int[] maxTrinketLight = {maxLightLevel};

                attachment.forEach((slotAccess, stack) -> {
                    if (!stack.isEmpty()) {
                        String itemId = BuiltInRegistries.ITEM
                                .getKey(stack.getItem())
                                .toString();

                        LightSource entry =
                                LightSource.getLightSources().get(itemId);

                        if (entry != null) {
                            if (entry.waterSensitive && player.isUnderWater()) {
                                return;
                            }

                            maxTrinketLight[0] = Math.max(
                                    maxTrinketLight[0],
                                    entry.luminance
                            );
                        }
                    }
                });

                maxLightLevel = maxTrinketLight[0];
            }
        }
        return maxLightLevel;
    }

    /**
     * Calculates the light level from dropped items within the configured radius.
     */
    private static int getDroppedItemLightLevel(ServerPlayer player) {
        int maxLightLevel = 0;
        ServerLevel world = player.level();
        double radius = ConfigCache.getDroppedItemRadius();
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ItemEntity> droppedItems = world.getEntitiesOfClass(ItemEntity.class, box, entity -> true);
        Vec3 playerEyePos = player.getEyePosition();

        for (ItemEntity itemEntity : droppedItems) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                LightSource entry = LightSource.getLightSources().get(itemId);
                if (entry != null) {
                    // Skip if water-sensitive and item is underwater
                    if (entry.waterSensitive && itemEntity.isUnderWater()) {
                        continue;
                    }
                    // Check line of sight with transparency
                    Vec3 itemPos = new Vec3(itemEntity.getX(), itemEntity.getY() + 0.5, itemEntity.getZ()); // Center of item entity
                    if (!RaycastUtil.hasLineOfSight(world, player, playerEyePos, itemPos)) {
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
    private static int getNearbyPlayerLightLevel(ServerPlayer player) {
        int maxLightLevel = 0;
        ServerLevel world = player.level();
        double radius = ConfigCache.getNearbyPlayerRadius();
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ServerPlayer> nearbyPlayers = world.getEntitiesOfClass(ServerPlayer.class, box,
                nearbyPlayer -> nearbyPlayer != player && !nearbyPlayer.isSpectator());
        Vec3 playerEyePos = player.getEyePosition();

        for (ServerPlayer nearbyPlayer : nearbyPlayers) {
            int lightLevel = getPlayerItemLightLevel(nearbyPlayer);
            if (lightLevel > 0) {
                // Check line of sight with transparency
                Vec3 nearbyPlayerEyePos = nearbyPlayer.getEyePosition();
                if (!RaycastUtil.hasLineOfSight(world, player, playerEyePos, nearbyPlayerEyePos)) {
                    continue;
                }
                if (lightLevel > maxLightLevel) {
                    maxLightLevel = lightLevel;
                }
            }
        }

        return maxLightLevel;
    }

}