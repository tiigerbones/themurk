package com.enchantedwisp.murk.effect;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.registry.DamageTypes;
import com.enchantedwisp.murk.util.ConfigCache;
import com.enchantedwisp.murk.util.tracker.PlayerLightTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class MurkGraspEffect extends MobEffect {
    private static final UUID SLOW_MOVEMENT_UUID = UUID.fromString("f3b2e1a0-9c7d-4b3e-8f2a-6c5d4e3b2a1f");
    private static final int RAMP_TIME_TICKS = 1200; // 60 seconds to reach max damage (20 ticks/sec)

    public MurkGraspEffect() {
        super(MobEffectCategory.HARMFUL, 0x1A1A1A); // Dark purple-gray color
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                String.valueOf(SLOW_MOVEMENT_UUID),
                -0.15, // 15% speed reduction
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            UUID id = player.getUUID();

            // Skip all logic in recovery persistence
            if (PlayerLightTracker.isDurationReduced(id)) {
                return;
            }

            // Increment exposure for ramp (every tick, cumulative across sessions if not reset)
            PlayerLightTracker.incrementEffectTicks(id);

            // Damage only at interval
            long worldTime = entity.level().getGameTime();
            int damageIntervalTicks = (int) (ConfigCache.getDamageInterval() * 20);
            if (worldTime % damageIntervalTicks != 0) {
                return;
            }

            // Calculate ramped damage over total exposure
            int effectTicks = PlayerLightTracker.getEffectTicks(id);
            float progress = Math.min(1.0f, (float) effectTicks / RAMP_TIME_TICKS);
            float damage = ConfigCache.getBaseDamage() + (ConfigCache.getMaxDamage() - ConfigCache.getBaseDamage()) * progress;

            // Apply Murk damage
            if (player.hurt(DamageTypes.of(player.level()), damage)) {
                TheMurk.LOGGER.debug("Applied Murk damage to {}: {} (progress={})", player.getName().getString(), damage, progress);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
    }
}