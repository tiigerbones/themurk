package com.enchantedwisp.murk.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Sounds {
    public static final SoundEvent MURK_WHISPERS = SoundEvent.of(Identifier.of("murk", "murk_whispers"));
    public static final SoundEvent MURK_VANISH = SoundEvent.of(Identifier.of("murk", "murk_vanish"));

    public static void register() {
        Registry.register(
                Registries.SOUND_EVENT,
                Identifier.of("murk", "murk_whispers"),
                MURK_WHISPERS
        );
        Registry.register(
                Registries.SOUND_EVENT,
                Identifier.of("murk", "murk_vanish"),
                MURK_VANISH
        );
    }
}