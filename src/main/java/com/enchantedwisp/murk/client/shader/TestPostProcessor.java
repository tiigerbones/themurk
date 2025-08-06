package com.enchantedwisp.murk.client.shader;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

public class TestPostProcessor extends PostProcessor {
    public static final TestPostProcessor INSTANCE = new TestPostProcessor();
    private static final Logger LOGGER = LoggerFactory.getLogger("murk_test_shader");

    static {
        INSTANCE.setActive(false); // Always active for testing
        LOGGER.info("ChromaticAberrationPostProcessor instantiated, set to inactive");
    }

    @Override
    public Identifier getPostChainLocation() {
        Identifier location = Identifier.of("murk", "test_shader");
        LOGGER.info("Returning test post chain location: {}", location);
        return location;
    }

    @Override
    public void init() {
        super.init();
        LOGGER.info("Initializing TestPostProcessor");
        if (postChain != null) {
            LOGGER.info("Test post chain loaded successfully for {}", getPostChainLocation());
        } else {
            LOGGER.error("Failed to load test post chain for {}", getPostChainLocation());
        }
    }

    @Override
    public void beforeProcess(MatrixStack matrixStack) {
        LOGGER.debug("TestPostProcessor beforeProcess called");
    }

    @Override
    public void afterProcess() {
        LOGGER.debug("TestPostProcessor afterProcess called");
    }
}