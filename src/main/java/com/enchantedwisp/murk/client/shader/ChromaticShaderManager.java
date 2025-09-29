package com.enchantedwisp.murk.client.shader;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ChromaticShaderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("murk:ChromaticShaderManager");

    private static PostEffectProcessor chromaticShader;
    private static float time = 0f;
    private static float intensity = 0f;

    private static final Identifier SHADER_ID = new Identifier("murk", "shaders/post/chromatic_aberration.json");

    /** Initialize the shader (call once on client setup) */
    public static void init() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            chromaticShader = new PostEffectProcessor(
                    client.getTextureManager(),
                    client.getResourceManager(),
                    client.getFramebuffer(),
                    SHADER_ID
            );
            chromaticShader.setupDimensions(
                    client.getWindow().getFramebufferWidth(),
                    client.getWindow().getFramebufferHeight()
            );
            LOGGER.info("Chromatic shader initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize chromatic shader", e);
        }
    }

    /** Set the effect intensity (0-1 range typically) */
    public static void setIntensity(float newIntensity) {
        intensity = newIntensity;
    }

    /** Update per-frame uniforms and render if active */
    public static void render(float tickDelta, boolean active) {
        if (chromaticShader == null || !active) return;

        time += tickDelta;

        try {
            Field passesField = PostEffectProcessor.class.getDeclaredField("passes");
            passesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<PostEffectPass> passes = (List<PostEffectPass>) passesField.get(chromaticShader);

            for (PostEffectPass pass : passes) {
                if (pass.getProgram().getUniformByNameOrDummy("time") != null) {
                    pass.getProgram().getUniformByNameOrDummy("time").set(time);
                }
                if (pass.getProgram().getUniformByNameOrDummy("intensity") != null) {
                    pass.getProgram().getUniformByNameOrDummy("intensity").set(intensity);
                }
                if (pass.getProgram().getUniformByNameOrDummy("cameraPos") != null) {
                    Vector3f camPos = MinecraftClient.getInstance()
                            .gameRenderer.getCamera().getPos().toVector3f();
                    pass.getProgram().getUniformByNameOrDummy("cameraPos").set(camPos);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Failed to access shader passes", e);
        }

        chromaticShader.render(tickDelta);
    }

    /** Update shader framebuffer size when window resizes */
    public static void onWindowResize(int width, int height) {
        if (chromaticShader != null) {
            chromaticShader.setupDimensions(width, height);
        }
    }

    /** Free resources when the client shuts down */
    public static void close() {
        if (chromaticShader != null) chromaticShader.close();
        chromaticShader = null;
        LOGGER.info("Chromatic shader closed");
    }
}
