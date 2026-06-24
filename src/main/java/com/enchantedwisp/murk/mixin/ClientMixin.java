package com.enchantedwisp.murk.mixin;

import com.enchantedwisp.murk.client.ScreenEffectManager;
import com.enchantedwisp.murk.registry.Effects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class ClientMixin {

    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void murk$renderShake(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            boolean resourcesLoaded,
            CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player != null && player.hasEffect(Effects.MURKS_GRASP)) {
            ScreenEffectManager.applyScreenShake(1.0f, 2);
        }
    }
}