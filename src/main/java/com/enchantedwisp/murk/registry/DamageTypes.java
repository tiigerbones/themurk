package com.enchantedwisp.murk.registry;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;

public class DamageTypes {
    public static final RegistryKey<DamageType> MURKS_GRASP_DAMAGE = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            Identifier.of("murk", "murks_grasp_damage")
    );

    public static void register() {
        // Damage type is registered via data (JSON), but we reference the key here for use
    }

    // Helper method to create DamageSource
    public static DamageSource of(World world) {
        return new DamageSource(
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(MURKS_GRASP_DAMAGE)
        );
    }
}