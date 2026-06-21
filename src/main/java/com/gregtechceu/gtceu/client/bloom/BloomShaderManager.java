package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.client.bloom.GameRendererAccessor;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.toma.configuration.config.validate.IConfigValueValidator;
import dev.toma.configuration.config.validate.IValidationResult;
import dev.toma.configuration.config.value.IConfigValueReadable;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@UtilityClass
public class BloomShaderManager {

    public static @UnknownNullability PostChain BLOOM_CHAIN = null;
    public static @UnknownNullability RenderTarget BLOOM_TARGET = null;

    @Getter
    private static @Nullable ShaderInstance rendertypeBloomShader;
    @Getter
    private static @Nullable ShaderInstance rendertypeEntityBloomShader;

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        // Add a validator & update listener for the bloom type config option
        // this path must match the config option's path exactly.
        ConfigHolder.INTERNAL_INSTANCE.getConfigValue("client.bloom.type", BloomType.class)
                .ifPresentOrElse(option -> option.addValidator(new BloomTypeConfigValidator()),
                        () -> GTCEu.LOGGER.warn(
                                "Could not initialize bloom type config update listener! The shaders will not update automatically when the config option is changed."));
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                GTCEu.id("rendertype_bloom"), DefaultVertexFormat.BLOCK),
                shader -> rendertypeBloomShader = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                GTCEu.id("rendertype_entity_bloom"), DefaultVertexFormat.NEW_ENTITY),
                shader -> rendertypeEntityBloomShader = shader);
    }

    /// @return whether the post effect was loaded successfully
    @ApiStatus.Internal
    public static boolean initPostShaders() {
        deinitPostShaders();

        // forcefully update availability on (re-)load
        bloomAvailable = updateBloomShaderAvailability();

        if (!isBloomAvailable()) return false;

        ResourceLocation id = null;

        switch (ConfigHolder.INSTANCE.client.bloom.type) {
            case UNITY -> id = GTCEu.id("shaders/post/bloom_unity.json");
            case UNREAL -> id = GTCEu.id("shaders/post/bloom_unreal.json");
            case DISABLED -> {
                return true;
            }
            // skip adding a default branch in favor of the if statement below
        }
        if (id == null) {
            GTCEu.LOGGER.error("Invalid bloom style {}", ConfigHolder.INSTANCE.client.bloom.type);
            ConfigHolder.INSTANCE.client.bloom.type = BloomType.DISABLED;
            return false;
        }

        try {
            Minecraft mc = Minecraft.getInstance();

            BLOOM_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), id);
            BLOOM_CHAIN.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            BLOOM_TARGET = BLOOM_CHAIN.getTempTarget("final");
            return true;
        } catch (Exception e) {
            GTCEu.LOGGER.error("Failed to {} shader {}:", e instanceof JsonSyntaxException ? "parse" : "load", id, e);
            BLOOM_CHAIN = null;
            BLOOM_TARGET = null;
            return false;
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
                ConfigHolder.INSTANCE.client.bloom.type != BloomType.DISABLED && isBloomAvailable();
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

    private static final class BloomTypeConfigValidator implements IConfigValueValidator<BloomType> {

        @Override
        public IValidationResult validate(BloomType newType, IConfigValueReadable<BloomType> configField) {
            if (!BloomShaderManager.initPostShaders()) {
                // failed to load post shaders

                Path gameDir = FMLPaths.GAMEDIR.get().toAbsolutePath();
                Path logFile = gameDir.resolve(Path.of("logs", "latest.log"));
                Component latestLogClickable = Component.literal(gameDir.relativize(logFile).toString())
                        .withStyle((style) -> style.withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,
                                        logFile.toFile().toString())));

                return IValidationResult.warning(Component.translatable(
                        "config.gtceu.option.bloomType.load_error", latestLogClickable));
            } else {
                // post shader loaded successfully
                return IValidationResult.success();
            }
        }
    }
}
