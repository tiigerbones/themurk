package com.enchantedwisp.murk.client.shader;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;
import com.enchantedwisp.murk.registry.Effects;
import net.minecraft.client.MinecraftClient;

public class ChromaticAberrationPostProcessor extends PostProcessor {
    public static final ChromaticAberrationPostProcessor INSTANCE = new ChromaticAberrationPostProcessor();
    private static final Logger LOGGER = LoggerFactory.getLogger("murk_shader");

    static {
        INSTANCE.setActive(false);
        LOGGER.info("ChromaticAberrationPostProcessor instantiated, set to inactive");
    }

    @Override
    public Identifier getPostChainLocation() {
        Identifier location = Identifier.of("murk", "chromatic_aberration");
        LOGGER.info("Returning post chain location: {}", location);
        return location;
    }

    @Override
    public void beforeProcess(MatrixStack matrixStack) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        boolean shouldBeActive = player != null && player.hasStatusEffect(Effects.MURKS_GRASP);
        if (shouldBeActive != isActive()) {
            setActive(shouldBeActive);
            LOGGER.info("Chromatic Aberration shader set to: {}", shouldBeActive ? "ON" : "OFF");
        }
    }

    @Override
    public void init() {
        super.init();
        LOGGER.info("Initializing ChromaticAberrationProcessor");
        if (postChain != null) {
            LOGGER.info("Chromatic Aberration chain loaded successfully for {}", getPostChainLocation());
        } else {
            LOGGER.error("Failed to load Chromatic Aberration chain for {}", getPostChainLocation());
        }
    }

    @Override
    public void afterProcess() {
        // No cleanup needed for now
    }
}