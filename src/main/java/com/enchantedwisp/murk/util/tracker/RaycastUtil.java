package com.enchantedwisp.murk.util.tracker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastUtil {
    /**
     * Checks if there is a clear line of sight between two points, respecting transparent blocks.
     */
    public static boolean hasLineOfSight(
            ServerLevel world,
            Entity entity,
            Vec3 start,
            Vec3 end
    ) {
        return world.clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.VISUAL,
                        ClipContext.Fluid.NONE,
                        entity
                )
        ).getType() == HitResult.Type.MISS;
    }
}