package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.registry.Effects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

@Mixin(InGameHud.class)
public class ClientMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void murk$renderShake(DrawContext context, float tickDelta, CallbackInfo ci) {
        PlayerEntity player = net.minecraft.client.MinecraftClient.getInstance().player;
        if (player != null && player.hasStatusEffect(Effects.MURKS_GRASP)) {
            // Apply subtle screen shake
            ScreenshakeInstance shake = new ScreenshakeInstance(20);
            shake.setEasing(Easing.SINE_IN_OUT); // Smooth easing for ramp-up and ramp-down
            shake.setIntensity(0.1f); // Subtle shake
            ScreenshakeHandler.addScreenshake(shake);
        }
    }
}