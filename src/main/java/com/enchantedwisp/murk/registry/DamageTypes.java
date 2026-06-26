package com.enchantedwisp.murk.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class DamageTypes {
    public static final ResourceKey<DamageType> MURKS_GRASP_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath("murk", "murks_grasp_damage")
    );

    public static void register() {
        // Damage type is registered via data (JSON), but we reference the key here for use
    }

    // Helper method to create DamageSource
    public static DamageSource of(Level world) {
        Holder<DamageType> entry = world.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .get(MURKS_GRASP_DAMAGE.identifier())
                .orElseThrow();

        return new DamageSource(entry);
    }
}