package com.gregtechceu.gtceu.client.shader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.shader.post.BloomType;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class GTShaders {

    public static final Minecraft mc = Minecraft.getInstance();

    public static PostChain BLOOM_CHAIN;
    public static BloomType BLOOM_TYPE;
    public static RenderTarget BLOOM_TARGET;

    public static Map<BlockPos, VertexBuffer> BLOOM_BUFFERS = new HashMap<>();
    public static Map<BlockPos, BufferBuilder> BLOOM_BUFFER_BUILDERS = new ConcurrentHashMap<>();
    public static Map<BlockPos, BufferBuilder.RenderedBuffer> RENDERED_BLOOM_BUFFERS = new HashMap<>();

    public static void onRegisterShaders(RegisterShadersEvent event) {
        // skip bloom target check here
        if (!ConfigHolder.INSTANCE.client.shader.useShader ||
                GTCEu.isIrisOculusLoaded() && IrisApi.getInstance().isShaderPackInUse()) {
            return;
        }

        initPostShaders();
    }

    private static void initPostShaders() {
        if (BLOOM_CHAIN != null) {
            BLOOM_CHAIN.close();
        }

        ResourceLocation id;

        switch (ConfigHolder.INSTANCE.client.shader.bloomStyle) {
            case 0 -> {
                id = GTCEu.id("shaders/post/bloom_gaussian.json");
                BLOOM_TYPE = BloomType.GAUSSIAN;
            }
            case 1 -> {
                id = GTCEu.id("shaders/post/bloom_unity.json");
                BLOOM_TYPE = BloomType.UNITY;
            }
            case 2 -> {
                id = GTCEu.id("shaders/post/bloom_unreal.json");
                BLOOM_TYPE = BloomType.UNREAL;
            }
            default -> {
                GTCEu.LOGGER.error("Invalid bloom style {}", ConfigHolder.INSTANCE.client.shader.bloomStyle);
                BLOOM_TYPE = BloomType.DISABLED;
                BLOOM_CHAIN = null;
                BLOOM_TARGET = null;
                return;
            }
        }

        try {
            BLOOM_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), id);
            BLOOM_CHAIN.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            BLOOM_TARGET = BLOOM_CHAIN.getTempTarget("final");
        } catch (IOException ioexception) {
            GTCEu.LOGGER.error("Failed to load shader: {}", id, ioexception);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        } catch (JsonSyntaxException jsonsyntaxexception) {
            GTCEu.LOGGER.error("Failed to parse shader: {}", id, jsonsyntaxexception);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        }
    }

    public static boolean allowedShader() {
        return ConfigHolder.INSTANCE.client.shader.useShader && BLOOM_TARGET != null &&
                !(GTCEu.isIrisOculusLoaded() && IrisApi.getInstance().isShaderPackInUse());
    }
}
