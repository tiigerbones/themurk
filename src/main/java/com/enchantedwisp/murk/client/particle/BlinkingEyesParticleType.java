package com.enchantedwisp.murk.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

@SuppressWarnings("deprecation")
public class BlinkingEyesParticleType extends ParticleType<BlinkingEyesParticleType> implements ParticleEffect {
    public static final Codec<BlinkingEyesParticleType> CODEC = Codec.unit(new BlinkingEyesParticleType());

    // Factory for legacy ParticleType constructor
    public static final ParticleEffect.Factory<BlinkingEyesParticleType> FACTORY =
            new ParticleEffect.Factory<>() {
                @Override
                public BlinkingEyesParticleType read(ParticleType<BlinkingEyesParticleType> type, PacketByteBuf buf) {
                    return new BlinkingEyesParticleType();
                }

                @Override
                public BlinkingEyesParticleType read(ParticleType<BlinkingEyesParticleType> type, StringReader reader) {
                    return new BlinkingEyesParticleType();
                }
            };

    public BlinkingEyesParticleType() {
        super(false, FACTORY); // old-style constructor requires Factory
    }

    @Override
    public Codec<BlinkingEyesParticleType> getCodec() {
        return CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return this;
    }

    @Override
    public void write(PacketByteBuf buf) {
        // no extra data to write
    }

    @Override
    public String asString() {
        return "blinking_eyes"; // match registry name
    }
}
