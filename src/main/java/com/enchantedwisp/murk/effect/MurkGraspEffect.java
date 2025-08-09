package com.enchantedwisp.murk.effect;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.config.MurkConfig;
import com.enchantedwisp.murk.registry.DamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.UUID;

import me.shedaniel.autoconfig.AutoConfig;

public class MurkGraspEffect extends StatusEffect {
    public static final String EFFECT_ID = "murks_grasp";
    private static final UUID SLOW_MOVEMENT_UUID = UUID.fromString("f3b2e1a0-9c7d-4b3e-8f2a-6c5d4e3b2a1f");
    private static final int MAX_SCALING_TICKS = 1200; // 60 seconds to reach max damage

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
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity player) {
            MurkConfig config;
            try {
                config = AutoConfig.getConfigHolder(MurkConfig.class).getConfig();
            } catch (Exception e) {
                TheMurk.LOGGER.error("Failed to load MurkConfig, using default values", e);
                config = new MurkConfig();
            }

            // Apply damage at configurable interval, only if effect duration is infinite
            StatusEffectInstance effect = player.getStatusEffect(this);
            int damageIntervalTicks = (int) (config.effect_damageInterval * 20); // Convert seconds to ticks
            if (effect != null && effect.getDuration() == -1 && entity.getWorld().getTime() % damageIntervalTicks == 0) {
                int duration = effect.getDuration();
                // For infinite duration (-1), treat as max scaling
                float progress = duration == -1 ? 1.0f : Math.min(1.0f, (float)(MAX_SCALING_TICKS - duration) / MAX_SCALING_TICKS);
                float damage = config.effect_baseDamage + (config.effect_maxDamage - config.effect_baseDamage) * progress;
                player.damage(DamageTypes.of(player.getWorld()), damage);
                TheMurk.LOGGER.debug("Applied {} damage to player {} with Murk's Grasp (progress: {})",
                        damage, player.getName().getString(), progress);
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient) {
            MurkConfig config;
            try {
                config = AutoConfig.getConfigHolder(MurkConfig.class).getConfig();
            } catch (Exception e) {
                TheMurk.LOGGER.error("Failed to load MurkConfig, using default values", e);
                config = new MurkConfig();
            }
            // Only send message and apply blindness if the player didn't already have the effect
            if (player.getStatusEffect(this) == null || Objects.requireNonNull(player.getStatusEffect(this)).getDuration() == -1) {
                if (config.effect_blindnessEnabled) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.BLINDNESS,
                            Objects.requireNonNull(player.getStatusEffect(this)).getDuration(),
                            0, // Amplifier 0 (base level)
                            false, // Not ambient
                            false // Hide particles
                    ));
                }
                // Send message only if warning text is enabled
                if (config.general_enableWarningText) {
                    player.sendMessageToClient(
                            Text.literal("You are gripped by Murk’s Grasp!").styled(style -> style.withColor(0xFF5555)),
                            true // Use action bar
                    );
                    TheMurk.LOGGER.debug("Sent Murk's Grasp action bar message to player {}", player.getName().getString());
                }
            }
        }
    }
}