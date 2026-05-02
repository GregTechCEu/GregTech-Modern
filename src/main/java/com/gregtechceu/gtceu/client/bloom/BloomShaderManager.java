package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.client.bloom.GameRendererAccessor;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class BloomShaderManager {

    public static @UnknownNullability PostChain BLOOM_CHAIN = null;
    public static @UnknownNullability RenderTarget BLOOM_TARGET = null;

    @Getter
    private static @Nullable ShaderInstance rendertypeBloomShader;
    @Getter
    private static @Nullable ShaderInstance rendertypeEntityBloomShader;

    @ApiStatus.Internal
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                GTCEu.id("rendertype_bloom"), DefaultVertexFormat.BLOCK),
                shader -> rendertypeBloomShader = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                GTCEu.id("rendertype_entity_bloom"), DefaultVertexFormat.NEW_ENTITY),
                shader -> rendertypeEntityBloomShader = shader);
    }

    @ApiStatus.Internal
    public static void initPostShaders() {
        deinitPostShaders();

        // forcefully update availability on (re-)load
        bloomAvailable = updateBloomShaderAvailability();

        if (!isBloomAvailable()) return;

        ResourceLocation id = null;

        switch (ConfigHolder.INSTANCE.client.bloom.bloomType) {
            case UNITY -> id = GTCEu.id("shaders/post/bloom_unity.json");
            case UNREAL -> id = GTCEu.id("shaders/post/bloom_unreal.json");
            case DISABLED -> {
                return;
            }
            // skip adding a default branch in favor of the if statement below
        }
        if (id == null) {
            GTCEu.LOGGER.error("Invalid bloom style {}", ConfigHolder.INSTANCE.client.bloom.bloomType);
            ConfigHolder.INSTANCE.client.bloom.bloomType = BloomAlgorithm.DISABLED;
            return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();

            BLOOM_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), id);
            BLOOM_CHAIN.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            BLOOM_TARGET = BLOOM_CHAIN.getTempTarget("final");
        } catch (IOException e) {
            GTCEu.LOGGER.error("Failed to load shader {}:", id, e);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        } catch (JsonSyntaxException e) {
            GTCEu.LOGGER.error("Failed to parse shader {}:", id, e);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        } catch (RuntimeException e) {
            GTCEu.LOGGER.error("Unexpected error loading shader {}:", id, e);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        }
    }

    private static void deinitPostShaders() {
        if (BLOOM_CHAIN != null) {
            BLOOM_CHAIN.close();
            BLOOM_TARGET.destroyBuffers();

            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
        }
    }

    public static boolean isBloomActive() {
        return BLOOM_CHAIN != null && BLOOM_TARGET != null &&
                ConfigHolder.INSTANCE.client.bloom.bloomType != BloomAlgorithm.DISABLED && isBloomAvailable();
    }

    @Getter
    private static boolean bloomAvailable = updateBloomShaderAvailability();

    @ApiStatus.Internal
    public static void updateShaderAvailability(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        int tick = ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getTick();
        // only update bloom availability once a second so every frame isn't bogged down with mod loaded checks
        if (tick % 20 != 0) return;

        bloomAvailable = updateBloomShaderAvailability();
    }

    private static boolean updateBloomShaderAvailability() {
        return !GTEarlyConfig.OPTIFINE_PRESENT &&
                !(GTCEu.Mods.isIrisOculusLoaded() && IrisCallWrapper.isShaderActive());
    }

    private static class IrisCallWrapper {

        private static boolean isShaderActive() {
            return IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
