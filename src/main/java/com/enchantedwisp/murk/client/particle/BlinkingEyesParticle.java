package com.enchantedwisp.murk.client.particle;

import com.enchantedwisp.murk.client.MurkClientState;
import com.enchantedwisp.murk.util.ConfigCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;

public class BlinkingEyesParticle extends SpriteBillboardParticle {
    /*
    private int stareTicks;
     */
    private boolean isFading;
    private float fadeAlpha;


    protected BlinkingEyesParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.setSpriteForAge(spriteProvider);
        this.maxAge = 500;
        this.gravityStrength = 0.0f; // No gravity
        this.collidesWithWorld = false; // Pass through blocks
        this.scale = 0.5f; // Adjust size as needed
        this.fadeAlpha = 1.0f;
        this.isFading = false;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        // Check if warning is still active or particle is disabled
        if (!MurkClientState.isWarningActive() || !ConfigCache.isEyesParticleEnabled()) {
            this.markDead(); // Instant despawn when warning ends
            return;
        }

        // Check light level at particle position
        int lightLevel = world.getLightLevel(LightType.BLOCK, BlockPos.ofFloored(this.x, this.y, this.z));
        if (lightLevel >= ConfigCache.getLightThreshold()) {
            if (!isFading) {
                isFading = true;
                this.maxAge = this.age + 20; // Fade over 1 second (20 ticks)
            }
        }

        // Handle fading
        if (isFading) {
            this.fadeAlpha = 1.0f - ((float)(this.age - (this.maxAge - 20)) / 20.0f);
            if (this.fadeAlpha <= 0) {
                this.markDead();
            }
        }
/*
        // Staring detection
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Vec3d playerPos = client.player.getEyePos();
            Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
            Vec3d lookVec = client.player.getRotationVec(1.0f);
            Vec3d toParticle = particlePos.subtract(playerPos).normalize();

            // Check if player is looking at particle (within 5 degrees)
            double dot = lookVec.dotProduct(toParticle);
            if (dot > 0.996) { // cos(5 degrees) ≈ 0.996
                stareTicks++;
                if (stareTicks >= 40 && (stareTicks - 40) % 80 == 0) { // Trigger every 4 seconds after initial 2 seconds
                    // chromatic aberration
                    ChromaticAberrationPostProcessor.INSTANCE.setActive(true); // Increase intensity
                }
            } else {
                stareTicks = 0;
                ChromaticAberrationPostProcessor.INSTANCE.setActive(false); // Reset intensity when not staring
            }
        }
 */
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        // Ensure particle faces the player (billboard)
        super.buildGeometry(vertexConsumer, camera, tickDelta);
        this.alpha = this.fadeAlpha; // Apply fade effect
    }

    public static class Factory implements ParticleFactory<BlinkingEyesParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(BlinkingEyesParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
            return new BlinkingEyesParticle(world, x, y, z, spriteProvider);
        }
    }
}