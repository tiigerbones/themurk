package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.effect.MurkGraspEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class Effects {
    public static Holder<MobEffect> MURKS_GRASP;

    public static void register() {
        MURKS_GRASP = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath("murk", "murks_grasp"),
                new MurkGraspEffect()
        );
    }
}