package com.enchantedwisp.murk.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class MurkGraspEffect extends StatusEffect {
    public static final String EFFECT_ID = "murks_grasp";
    private static final UUID SLOW_MOVEMENT_UUID = UUID.fromString("f3b2e1a0-9c7d-4b3e-8f2a-6c5d4e3b2a1f");

    public MurkGraspEffect() {
        super(StatusEffectCategory.HARMFUL, 0x1A1A1A); // Dark purple-gray color
        this.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                String.valueOf(SLOW_MOVEMENT_UUID),
                -0.15, // 15% speed reduction
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Apply slowness via attribute (handled by super)
        // Apply blindness effect
        if (entity instanceof PlayerEntity) {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS,
                    99, //
                    0, // Amplifier 0 (base level)
                    false, // Not ambient
                    false // Hide particles
            ));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(
                    Text.literal("You are gripped by Murk’s Grasp!")
            );
        }
    }
}