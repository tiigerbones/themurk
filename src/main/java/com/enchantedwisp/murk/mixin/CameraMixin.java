package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.client.ScreenEffectManager;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    protected void setRotation(float yaw, float pitch) {}

    @Shadow
    public float getYaw() { return 0; }

    @Shadow
    public float getPitch() { return 0; }

    @Inject(
        method = "update(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;ZZF)V",
        at = @At("TAIL")
    )
    private void injectScreenshake(World area, Entity entity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        float[] offsets = ScreenEffectManager.getCameraShakeOffsets();
        this.setRotation(
                this.getYaw() + offsets[0],
                this.getPitch() + offsets[1]
        );
    }
}
