package com.enchantedwisp.murk.client.sound;

import com.enchantedwisp.murk.registry.Effects;
import com.enchantedwisp.murk.registry.Sounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PhaseSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public PhaseSoundInstance(Player player) {
        super(Sounds.MURK_WHISPERS, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true; // Enable looping
        this.delay = 0; // No delay between loops
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (!this.player.isAlive() || !this.player.hasEffect(Effects.MURKS_GRASP)) {
            this.stop();
        } else {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }
}