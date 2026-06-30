package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.effect.MurkGraspEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class Effects {
    public static final String EFFECT_ID = "murks_grasp";
    public static final MobEffect MURKS_GRASP = new MurkGraspEffect();

    public static void register() {
        Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.tryBuild("murk", EFFECT_ID),
                MURKS_GRASP
        );
    }
}