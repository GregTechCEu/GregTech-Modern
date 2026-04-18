package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;

import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelEventHelper {

    @ApiStatus.Internal
    public static final List<EventListenerHolder<?>> EVENT_LISTENERS = new ArrayList<>();
    @ApiStatus.Internal
    public static final Map<ResourceLocation, TextureAtlasSprite> CTM_SPRITE_CACHE = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public static final Multimap<ResourceLocation, Material> SCRAPED_TEXTURES = HashMultimap.create();
    @ApiStatus.Internal
    public static final Object2BooleanMap<ResourceLocation> WRAPPED_MODELS = new Object2BooleanLinkedOpenHashMap<>();

    @ApiStatus.Internal
    public static void textureScraped(ResourceLocation modelLocation, Material material) {
        SCRAPED_TEXTURES.put(modelLocation, material);
    }

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload,
                                                          AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add(new EventListenerHolder<>(listener, removeOnReload));
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload, final ResourceLocation atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        registerAtlasStitchedEventListener(removeOnReload, event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    public static void registerBakeEventListener(boolean removeOnReload,
                                                 AssetEventListener.BakedModelReplacement listener) {
        EVENT_LISTENERS.add(new EventListenerHolder<>(listener, removeOnReload));
    }

    public static void registerAddModelsEventListener(boolean removeOnReload,
                                                      AssetEventListener.RegisterAdditional listener) {
        EVENT_LISTENERS.add(new EventListenerHolder<>(listener, removeOnReload));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceManagerReloadListener() {

            @Override
            public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
                EVENT_LISTENERS.removeIf(EventListenerHolder::removeOnReload);
            }
        });
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            // Cache all textures' CTM metadata
            // TODO lazy
            CTM_SPRITE_CACHE.clear();
            for (ResourceLocation location : event.getAtlas().getTextureLocations()) {
                ResourceLocation absLoc = LDLMetadataSection.spriteToAbsolute(location);
                LDLMetadataSection section = LDLMetadataSection.getMetadata(absLoc);
                if (section.connection != null) {
                    TextureAtlasSprite ctmSprite = event.getAtlas().getSprite(section.connection);
                    CTM_SPRITE_CACHE.put(location, ctmSprite);
                }
            }
        }

        TextureAtlas atlas = event.getAtlas();
        if (atlas.location() == TextureAtlas.LOCATION_BLOCKS) {
            MachineModel.initSprites(atlas);
            ICoverableRenderer.initSprites(atlas);
        }

        for (var listener : EVENT_LISTENERS) {
            if (!(listener.listener instanceof AssetEventListener<?> assetEventListener)) continue;

            Class<?> eventClass = assetEventListener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<TextureStitchEvent.Post>) listener.listener).accept(event);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // don't process baked model replacement here if modernfix is loaded, as
        // GTModernFixIntegration#onBakedModelLoad does the same thing & it's always called
        if (GTCEu.Mods.isModernFixLoaded()) return;

        for (var entry : event.getModels().entrySet()) {
            BakedModel model = entry.getValue();

            // process all model replacers
            for (var listener : EVENT_LISTENERS) {
                if (!(listener.listener instanceof AssetEventListener.BakedModelReplacement modelReplacement)) continue;
                model = modelReplacement.modifyBakedModel(entry.getKey(), model,
                        event.getModelBakery().getModel(entry.getKey()), event.getModelBakery());
            }
            entry.setValue(model);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (var listener : EVENT_LISTENERS) {
            if (!(listener.listener instanceof AssetEventListener<?> assetEventListener)) continue;

            Class<?> eventClass = assetEventListener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.RegisterAdditional>) listener.listener).accept(event);
            }
        }
    }

    @ApiStatus.Internal
    public record EventListenerHolder<T>(T listener, boolean removeOnReload) {}
}
