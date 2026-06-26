package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.client.ScreenEffectManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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
    public float yRot() {
        return 0;
    }

    @Shadow
    public float xRot() {
        return 0;
    }

    @Inject(
            method = "update",
            at = @At("TAIL")
    )
    private void injectScreenshake(DeltaTracker deltaTracker, CallbackInfo ci) {
        float[] offsets = ScreenEffectManager.getCameraShakeOffsets();

        this.setRotation(
                this.yRot() + offsets[0],
                this.xRot() + offsets[1]
        );
    }
}