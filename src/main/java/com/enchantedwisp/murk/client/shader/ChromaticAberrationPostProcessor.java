package com.enchantedwisp.murk.client.shader;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

public class ChromaticAberrationPostProcessor extends PostProcessor {
    public static final ChromaticAberrationPostProcessor INSTANCE = new ChromaticAberrationPostProcessor();

    static {
        INSTANCE.setActive(false);
    }

    @Override
    public Identifier getPostChainLocation() {
        return Identifier.of("murk", "chromatic_aberration");
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void beforeProcess(MatrixStack matrixStack) {
    }

    @Override
    public void afterProcess() {
    }
}
