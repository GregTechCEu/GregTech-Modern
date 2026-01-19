package com.gregtechceu.gtceu.client.shader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.bloom.BloomAlgorithm;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class GTShaders {

    public static PostChain BLOOM_CHAIN = null;
    public static BloomAlgorithm BLOOM_TYPE = ConfigHolder.INSTANCE.client.shader.bloomAlgorithm;
    public static RenderTarget BLOOM_TARGET = null;

    public static void onRegisterShaders(RegisterShadersEvent event) {
        if (!canLoadBloomShader()) {
            return;
        }

        initPostShaders();
    }

    private static void initPostShaders() {
        if (BLOOM_CHAIN != null) {
            BLOOM_CHAIN.close();
        }

        ResourceLocation id = null;

        switch (BLOOM_TYPE) {
            case UNITY -> id = GTCEu.id("shaders/post/bloom_unity.json");
            case UNREAL -> id = GTCEu.id("shaders/post/bloom_unreal.json");
            case DISABLED -> {
                return;
            }
            // skip adding a default branch in favor of the if statement below
        }
        if (id == null) {
            GTCEu.LOGGER.error("Invalid bloom style {}", ConfigHolder.INSTANCE.client.shader.bloomAlgorithm);
            ConfigHolder.INSTANCE.client.shader.bloomAlgorithm = BloomAlgorithm.DISABLED;
            BLOOM_TYPE = BloomAlgorithm.DISABLED;
            return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();

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

    public static boolean canUseBloomShader() {
        return BLOOM_CHAIN != null && BLOOM_TARGET != null && canLoadBloomShader();
    }

    private static boolean canLoadBloomShader() {
        return ConfigHolder.INSTANCE.client.shader.enableBloom &&
                !GTCEu.isModLoaded(GTValues.MODID_OPTIFINE) &&
                !(GTCEu.Mods.isIrisOculusLoaded() && IrisCallWrapper.isShaderActive());
    }

    private static class IrisCallWrapper {

        private static boolean isShaderActive() {
            return IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
