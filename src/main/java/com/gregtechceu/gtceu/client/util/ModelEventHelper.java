package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;

import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
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

    private static final Multimap<ResourceLocation, Material> SCRAPED_TEXTURES = HashMultimap.create();
    @ApiStatus.Internal
    public static final Object2BooleanMap<ResourceLocation> WRAPPED_MODELS = new Object2BooleanOpenHashMap<>();

    @ApiStatus.Internal
    public static void markTextureUsedForModel(ResourceLocation modelLocation, Material material) {
        SCRAPED_TEXTURES.put(modelLocation, material);
    }

    @ApiStatus.Internal
    public static Collection<Material> getModelUsedCTMTextures(ResourceLocation modelLocation) {
        return SCRAPED_TEXTURES.get(modelLocation);
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
            public void onResourceManagerReload(ResourceManager resourceManager) {
                EVENT_LISTENERS.removeIf(EventListenerHolder::removeOnReload);
            }
        });
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
        TextureAtlas atlas = event.getAtlas();
        if (atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
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
