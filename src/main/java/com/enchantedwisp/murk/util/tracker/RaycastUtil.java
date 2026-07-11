package com.enchantedwisp.murk.util.tracker;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastUtil {
    /**
     * Checks if there is a clear line of sight between two points, respecting transparent blocks.
     */
    public static boolean hasLineOfSight(
            ServerWorld world,
            Entity entity,
            Vec3d start,
            Vec3d end
    ) {
        return world.raycast(
                new RaycastContext(
                        start,
                        end,
                        RaycastContext.ShapeType.VISUAL,
                        RaycastContext.FluidHandling.NONE,
                        entity
                )
        ).getType() == HitResult.Type.MISS;
    }
}