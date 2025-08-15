package com.enchantedwisp.murk.registry;

import com.enchantedwisp.murk.TheMurk;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Sounds {
    public static final SoundEvent MURK_WHISPERS = register("murk_whispers");
    public static final SoundEvent MURK_VANISH = register("murk_vanish");

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.of(TheMurk.MOD_ID, id);
        SoundEvent soundEvent = SoundEvent.of(identifier);
        return Registry.register(Registries.SOUND_EVENT, identifier, soundEvent);
    }

    public static void init() {
        TheMurk.LOGGER.info("Registering sounds for The Murk");
    }
}