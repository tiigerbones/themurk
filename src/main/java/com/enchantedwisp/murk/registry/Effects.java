package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.effect.MurkGraspEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class Effects {
    public static RegistryEntry<StatusEffect> MURKS_GRASP;

    public static void register() {
        MURKS_GRASP = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of("murk", "murks_grasp"),
                new MurkGraspEffect()
        );
    }
}