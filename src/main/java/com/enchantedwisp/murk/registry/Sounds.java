package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.TheMurk;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class Sounds {
    public static final SoundEvent MURK_WHISPERS = register("murk_whispers");
    public static final SoundEvent MURK_VANISH = register("murk_vanish");

    private static SoundEvent register(String id) {
        ResourceLocation identifier = ResourceLocation.tryBuild(TheMurk.MOD_ID, id);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(identifier);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, soundEvent);
    }

    public static void init() {
        TheMurk.LOGGER.info("Registering sounds for The Murk");
    }
}