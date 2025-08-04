package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.registry.Effects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;
import team.lodestar.lodestone.systems.easing.Easing;

@Mixin(InGameHud.class)
public class ClientMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void murk$renderShake(DrawContext context, float tickDelta, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.hasStatusEffect(Effects.MURKS_GRASP)) {
            // Apply subtle screen shake
            ScreenshakeInstance shake = new ScreenshakeInstance(18);
            shake.setIntensity(0.01f); // Reduced intensity
            ScreenshakeHandler.addScreenshake(shake);
        }
    }
}