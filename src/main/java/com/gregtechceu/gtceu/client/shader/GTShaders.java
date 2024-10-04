package com.gregtechceu.gtceu.client.shader;

import com.google.gson.JsonSyntaxException;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.shader.post.BloomType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class GTShaders {

    public static final Minecraft mc = Minecraft.getInstance();

    public static ShaderInstance BLOOM_SHADER;

    public static PostChain BLOOM_CHAIN;
    public static BloomType BLOOM_TYPE;
    public static RenderTarget BLOOM_TARGET;

    public static ShaderInstance getBloomShader() {
        return BLOOM_SHADER;
    }

    @SubscribeEvent
    public static void onRenderTickStart(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (BLOOM_CHAIN != null) {
            BLOOM_CHAIN.process(event.renderTickTime);
        }
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        if (!allowedShader()) {
            return;
        }

        ResourceProvider resourceProvider = event.getResourceProvider();

        try {
            event.registerShader(
                    new ShaderInstance(resourceProvider, GTCEu.id("rendertype_bloom"), DefaultVertexFormat.BLOCK),
                    shader -> BLOOM_SHADER = shader);
        } catch (IOException ioException) {
            GTCEu.LOGGER.error("Failed to load shader: gtceu:rendertype_bloom", ioException);
            BLOOM_SHADER = null;
        }

        initPostShaders();
    }

    public static void initPostShaders() {
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
        return ConfigHolder.INSTANCE.client.shader.useShader && !GTCEu.isIrisOculusLoaded();
    }

    public static float getITime(float pPartialTicks) {
        if (mc.level == null) {
            return System.currentTimeMillis() % 1200000 / 1000f;
        } else {
            return ((mc.level.getGameTime() % 24000) + pPartialTicks) / 20f;
        }
    }
}
