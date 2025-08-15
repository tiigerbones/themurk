package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.TheMurk;
import com.enchantedwisp.murk.client.particle.BlinkingEyesParticle;
import com.enchantedwisp.murk.client.particle.BlinkingEyesParticleType;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Particles {
    public static final ParticleType<BlinkingEyesParticleType> BLINKING_EYES = new BlinkingEyesParticleType();

    public static void register() {
        Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(TheMurk.MOD_ID, "blinking_eyes"),
                BLINKING_EYES
        );
    }

    public static void registerClient() {
        ParticleFactoryRegistry.getInstance().register(BLINKING_EYES, BlinkingEyesParticle.Factory::new);
    }
}