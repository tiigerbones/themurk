package com.enchantedwisp.murk.effect;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
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

import java.util.Objects;
import java.util.UUID;

import me.shedaniel.autoconfig.AutoConfig;

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
        // Server-side: Only apply slowness (handled by super)
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player && !entity.getWorld().isClient) {
            MurkConfig config;
            try {
                config = AutoConfig.getConfigHolder(MurkConfig.class).getConfig();
            } catch (Exception e) {
                TheMurk.LOGGER.error("Failed to load MurkConfig, using default values", e);
                config = new MurkConfig(); // Fallback to default config
            }
            // Only send message and apply blindness if the player didn't already have the effect
            if (player.getStatusEffect(this) == null || Objects.requireNonNull(player.getStatusEffect(this)).getDuration() == -1) {
                if (config.blindnessEnabled) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.BLINDNESS,
                            Objects.requireNonNull(player.getStatusEffect(this)).getDuration(),
                            0, // Amplifier 0 (base level)
                            false, // Not ambient
                            false // Hide particles
                    ));
                }
                // Send message only if warning text is enabled
                if (config.enableWarningText) {
                    player.sendMessage(
                            Text.literal("You are gripped by Murk’s Grasp!").styled(style -> style.withColor(0xFF5555)),
                            false
                    );
                }
            }
        }
    }
}