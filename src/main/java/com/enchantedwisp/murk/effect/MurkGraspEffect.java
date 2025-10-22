package com.enchantedwisp.murk.effect;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.registry.DamageTypes;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

import java.util.UUID;

public class MurkGraspEffect extends StatusEffect {
    private static final UUID SLOW_MOVEMENT_UUID = UUID.fromString("f3b2e1a0-9c7d-4b3e-8f2a-6c5d4e3b2a1f");
    private static final int RAMP_TIME_TICKS = 1200; // 60 seconds to reach max damage (20 ticks/sec)

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
        if (!entity.getWorld().isClient && entity instanceof ServerPlayerEntity player) {
            UUID id = player.getUuid();

            // Skip all logic in recovery persistence
            if (PlayerLightTracker.isDurationReduced(id)) {
                return;
            }

            // Increment exposure for ramp (every tick, cumulative across sessions if not reset)
            PlayerLightTracker.incrementEffectTicks(id);

            // Damage only at interval
            long worldTime = entity.getWorld().getTime();
            int damageIntervalTicks = (int) (ConfigCache.getDamageInterval() * 20);
            if (worldTime % damageIntervalTicks != 0) {
                return;
            }

            // Calculate ramped damage over total exposure
            int effectTicks = PlayerLightTracker.getEffectTicks(id);
            float progress = Math.min(1.0f, (float) effectTicks / RAMP_TIME_TICKS);
            float damage = ConfigCache.getBaseDamage() + (ConfigCache.getMaxDamage() - ConfigCache.getBaseDamage()) * progress;

            // Apply Murk damage
            if (player.damage(DamageTypes.of(player.getWorld()), damage)) {
                TheMurk.LOGGER.debug("Applied Murk damage to {}: {} (progress={})", player.getName().getString(), damage, progress);
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
    }
}