package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.client.ScreenEffectManager;
import com.enchantedwisp.murk.registry.Effects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ClientMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void murk$renderShake(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null && player.hasStatusEffect(Effects.MURKS_GRASP)) {
            ScreenEffectManager.applyScreenShake(1.0f, 2);
        }
    }
}
