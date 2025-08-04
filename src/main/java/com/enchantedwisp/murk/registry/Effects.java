package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.effect.MurkGraspEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Effects {
    public static final StatusEffect MURKS_GRASP = new MurkGraspEffect();

    public static void register() {
        Registry.register(
                Registries.STATUS_EFFECT,
                Identifier.of("murk", MurkGraspEffect.EFFECT_ID),
                MURKS_GRASP
        );
    }
}