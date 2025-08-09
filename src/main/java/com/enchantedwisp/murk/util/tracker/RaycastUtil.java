package com.enchantedwisp.murk.util.tracker;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastUtil {
    /**
     * Checks if there is a clear line of sight between two points, respecting transparent blocks.
     */
    public static boolean hasLineOfSight(ServerWorld world, Vec3d start, Vec3d end) {
        RaycastContext context = new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.VISUAL, // Use VISUAL to respect transparent blocks like glass
                RaycastContext.FluidHandling.NONE,
                null
        );

        BlockHitResult hitResult = world.raycast(context);
        if (hitResult.getType() == BlockHitResult.Type.MISS) {
            return false; // No blocks hit, clear line of sight
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
                return true;
            }
        }
        return false; // All blocks are transparent or air
    }
}