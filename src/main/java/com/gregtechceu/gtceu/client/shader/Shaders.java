package com.gregtechceu.gtceu.client.shader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class Shaders {

    public static Minecraft mc;

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
        // int lastID = GlStateManager.getBoundFramebuffer();

        fbo.bindWrite(true);

        shader.safeGetUniform("u_resolution").set(fbo.width, fbo.height);
        if (uniformApplicator != null) {
            uniformApplicator.accept(shader);
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        buffer.vertex(-1, 1, 0).color(0xFFFFFFFF).uv(0, 0).endVertex();
        buffer.vertex(-1, -1, 0).color(0xFFFFFFFF).uv(0, 1).endVertex();
        buffer.vertex(1, -1, 0).color(0xFFFFFFFF).uv(1, 1).endVertex();
        buffer.vertex(1, 1, 0).color(0xFFFFFFFF).uv(1, 0).endVertex();
        tesselator.end();

        // RenderSystem.viewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());

        // GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastID);
        fbo.bindWrite(false);
        return fbo;
    }
}
