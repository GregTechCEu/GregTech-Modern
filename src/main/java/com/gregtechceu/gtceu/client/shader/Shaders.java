package com.gregtechceu.gtceu.client.shader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class Shaders {

    public static Minecraft mc;
    private final static Map<ShaderInstance, Program> FULL_IMAGE_PROGRAMS;

    public static ShaderInstance IMAGE;
    // public static ShaderObject BLACK_HOLE;

    public static ShaderInstance BLOOM_COMBINE;

    public static ShaderInstance BLUR;

    // Unity
    public static ShaderInstance DOWN_SAMPLING;
    public static ShaderInstance UP_SAMPLING;

    // Unreal
    public static ShaderInstance S_BLUR;
    public static ShaderInstance COMPOSITE;

    static {
        mc = Minecraft.getInstance();
        FULL_IMAGE_PROGRAMS = new HashMap<>();
        if (allowedShader()) {
            initShaders();
        }
    }

    public static void initShaders() {
        IMAGE = initShader(IMAGE, GTCEu.id("image"));
        // BLACK_HOLE = initShader(BLACK_HOLE, FRAGMENT, GTCEu.id("black_hole"));
        BLOOM_COMBINE = initShader(BLOOM_COMBINE, GTCEu.id("bloom_combine"));
        BLUR = initShader(BLUR, GTCEu.id("blur"));
        DOWN_SAMPLING = initShader(DOWN_SAMPLING, GTCEu.id("down_sampling"));
        UP_SAMPLING = initShader(UP_SAMPLING, GTCEu.id("up_sampling"));
        S_BLUR = initShader(S_BLUR, GTCEu.id("seperable_blur"));
        COMPOSITE = initShader(COMPOSITE, GTCEu.id("composite"));
        FULL_IMAGE_PROGRAMS.clear();
    }

    private static ShaderInstance initShader(ShaderInstance object, ResourceLocation location) {
        unloadShader(object);
        try {
            return loadShader(location);
        } catch (Exception exception) {
            GTCEu.LOGGER.error("error while loading shader {}", location, exception);
        }
        return null;
    }

    public static ShaderInstance loadShader(ResourceLocation resourceLocation) throws IOException {
        return new ShaderInstance(Minecraft.getInstance().getResourceManager(), resourceLocation, DefaultVertexFormat.POSITION_TEX);
    }

    public static void unloadShader(ShaderInstance shaderObject) {
        if (shaderObject != null) {
            shaderObject.close();
        }
    }

    public static boolean allowedShader() {
        return ConfigHolder.INSTANCE.client.shader.useShader;
    }

    public static RenderTarget renderFullImageInFBO(RenderTarget fbo, ShaderInstance shader,
                                                    Consumer<ShaderInstance> uniformApplicator) {
        if (fbo == null || shader == null || !allowedShader()) return fbo;
        // int lastID = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        fbo.bindWrite(true);

        Program program = FULL_IMAGE_PROGRAMS.get(shader);
        if (program == null) {
            program = shader.getFragmentProgram();
            FULL_IMAGE_PROGRAMS.put(shader, program);
        }

        shader.safeGetUniform("u_resolution").set(fbo.width, fbo.height);
        if (uniformApplicator != null) {
            uniformApplicator.accept(shader);
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        buffer.vertex(-1, 1, 0).color(0xFFFFFFFF).uv(0, 0).uv2(LightTexture.FULL_BRIGHT).normal(0.0f, 0.0f, 1.0f).endVertex();
        buffer.vertex(-1, -1, 0).color(0xFFFFFFFF).uv(0, 1).uv2(LightTexture.FULL_BRIGHT).normal(0.0f, 0.0f, 1.0f).endVertex();
        buffer.vertex(1, -1, 0).color(0xFFFFFFFF).uv(1, 1).uv2(LightTexture.FULL_BRIGHT).normal(0.0f, 0.0f, 1.0f).endVertex();
        buffer.vertex(1, 1, 0).color(0xFFFFFFFF).uv(1, 0).uv2(LightTexture.FULL_BRIGHT).normal(0.0f, 0.0f, 1.0f).endVertex();
        tesselator.end();

        shader.clear();
        // GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

        // OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
        fbo.unbindWrite();
        return fbo;
    }
}
