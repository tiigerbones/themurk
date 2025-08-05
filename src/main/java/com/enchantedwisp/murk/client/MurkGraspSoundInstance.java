package com.enchantedwisp.murk.client;

import com.enchantedwisp.murk.registry.Sounds;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class MurkGraspSoundInstance extends MovingSoundInstance {
    private final PlayerEntity player;

    public MurkGraspSoundInstance(PlayerEntity player) {
        super(Sounds.MURK_WHISPERS, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true; // Enable looping
        this.repeatDelay = 0; // No delay between loops
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (!this.player.isAlive() || !this.player.hasStatusEffect(com.enchantedwisp.murk.registry.Effects.MURKS_GRASP)) {
            this.setDone(); // Stop sound if player is dead or effect is gone
        } else {
            // Update sound position to follow the player
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }
}