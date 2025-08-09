package com.enchantedwisp.murk.client.shader;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

public class ChromaticAberrationPostProcessor extends PostProcessor {
    public static final ChromaticAberrationPostProcessor INSTANCE = new ChromaticAberrationPostProcessor();
    private static final Logger LOGGER = LoggerFactory.getLogger("murk_chromatic_aberration");
    static {
        INSTANCE.setActive(false);
    }

    @Override
    public Identifier getPostChainLocation() {
        Identifier location = Identifier.of("murk", "chromatic_aberration");

        LOGGER.info("Returning Chromatic Aberration chain location: {}", location);
        return location;
    }

    @Override
    public void init() {
        super.init();
        /* LOG FOR DEV
        LOGGER.info("Initializing ChromaticAberrationPostProcessor");
         */
        if (postChain != null) {
            LOGGER.info("Chromatic Aberration chain loaded successfully for {}", getPostChainLocation());
        } else {
            LOGGER.error("Failed to load Chromatic Aberration chain for {}", getPostChainLocation());
        }
    }

    @Override
    public void beforeProcess(MatrixStack matrixStack) {
    }

    @Override
    public void afterProcess() {
    }
}