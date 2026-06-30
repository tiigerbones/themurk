package com.enchantedwisp.murk.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class DamageTypes {
    public static final ResourceKey<DamageType> MURKS_GRASP_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.tryBuild("murk", "murks_grasp_damage")
    );

    public static void register() {
        // Damage type is registered via data (JSON), but we reference the key here for use
    }

    // Helper method to create DamageSource
    public static DamageSource of(Level world) {
        return new DamageSource(
                world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MURKS_GRASP_DAMAGE)
        );
    }
}