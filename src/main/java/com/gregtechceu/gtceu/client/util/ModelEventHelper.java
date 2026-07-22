package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.ctm.CTMBakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.core.mixins.ReloadableResourceManagerAccessor;
import com.gregtechceu.gtceu.integration.modernfix.GTModernFixIntegration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
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
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
@UtilityClass
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelEventHelper {

    @ApiStatus.Internal
    public record EventListenerHolder<T>(T listener, boolean removeOnReload) {}

    @ApiStatus.Internal
    public static final List<EventListenerHolder<?>> EVENT_LISTENERS = new ArrayList<>();
    @ApiStatus.Internal
    public static final Map<ResourceLocation, TextureAtlasSprite> CTM_SPRITE_CACHE = new ConcurrentHashMap<>();

    private static final Multimap<ResourceLocation, Material> SCRAPED_TEXTURES = HashMultimap.create();
    private static final Object2BooleanMap<ResourceLocation> WRAPPED_MODELS = new Object2BooleanOpenHashMap<>();

    @ApiStatus.Internal
    public static void markTextureUsedForModel(ResourceLocation modelLocation, Material material) {
        SCRAPED_TEXTURES.put(modelLocation, material);
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

    private static final AtomicInteger reloadCounter = new AtomicInteger(0);

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        ((ReloadableResourceManagerAccessor) Minecraft.getInstance().getResourceManager()).getListeners()
                .add(0, (ResourceManagerReloadListener) resourceManager -> {
                    if (reloadCounter.addAndGet(1) > 1) {
                        EVENT_LISTENERS.removeIf(EventListenerHolder::removeOnReload);
                    }

                    CTM_SPRITE_CACHE.clear();
                    WRAPPED_MODELS.clear();
                    SCRAPED_TEXTURES.clear();
                    TextureMetadataHelper.invalidateCaches();
                });
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
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
        // don't process baked model replacement here if ModernFix is loaded & dynamic resources is enabled
        if (GTCEu.Mods.isModernFixLoaded() && GTModernFixIntegration.isDynamicResourcesEnabled()) return;

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

    // INTERNAL ASSET RELOAD LISTENER REGISTRATION

    @ApiStatus.Internal
    public static void initInternalAssetReloadListeners() {
        registerAtlasStitchedEventListener(false, TextureAtlas.LOCATION_BLOCKS, event -> {
            TextureAtlas atlas = event.getAtlas();
            // Cache all textures' CTM metadata
            // TODO lazy
            for (ResourceLocation location : atlas.getTextureLocations()) {
                var sec = TextureMetadataHelper.getMetadataFromRelativeLocation(location);
                sec.ifPresent(section -> {
                    if (section.connectionTexture() != null) {
                        TextureAtlasSprite ctmSprite = atlas.getSprite(section.connectionTexture());
                        CTM_SPRITE_CACHE.put(location, ctmSprite);
                    }
                });
            }

            MachineModel.initSprites(atlas);
            ICoverableRenderer.initSprites(atlas);
        });

        // register CTM model wrapper
        ModelEventHelper.registerBakeEventListener(false, (rl, baked, rootModel, modelBakery) -> {
            if (baked.isCustomRenderer()) {
                // Nothing we can add to builtin models
                return baked;
            }
            // do not register automatic CTM for machine models, they handle it themselves
            if (baked instanceof MachineModel) {
                return baked;
            }

            if (!(rl instanceof ModelResourceLocation) || rootModel == null || baked instanceof CTMBakedModel<?>) {
                return baked;
            }
            Deque<ResourceLocation> dependencies = new ArrayDeque<>();
            Set<ResourceLocation> seenModels = new HashSet<>();
            dependencies.push(rl);
            seenModels.add(rl);

            boolean shouldWrap = WRAPPED_MODELS.getOrDefault(rl, false);
            if (WRAPPED_MODELS.containsKey(rl)) {
                // shortcut if the model's already been checked
                if (shouldWrap) return new CTMBakedModel<>(baked);
                else return baked;
            }
            // Breadth-first loop through dependencies
            // exiting as soon as a CTM texture is found, and skipping duplicates/cycles
            PARENT_LOOP:
            while (!shouldWrap && !dependencies.isEmpty()) {
                ResourceLocation dependencyName = dependencies.pop();
                UnbakedModel unbaked;
                try {
                    unbaked = dependencyName == rl ? rootModel : modelBakery.getModel(dependencyName);
                } catch (Exception e) {
                    continue;
                }
                try {
                    // have to copy because the set is updated during this loop
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                    Set<Material> textures = new HashSet<>(SCRAPED_TEXTURES.get(dependencyName));
                    for (Material tex : textures) {
                        if (TextureMetadataHelper.getMetadata(tex).isPresent()) {
                            // At least one texture has CTM metadata, so we should wrap this model
                            shouldWrap = true;
                            break PARENT_LOOP;
                        }
                    }
                    // shouldWrap is always false here because of the `break` above
                    for (ResourceLocation newDep : unbaked.getDependencies()) {
                        if (seenModels.add(newDep)) {
                            dependencies.push(newDep);
                        }
                    }
                } catch (Exception e) {
                    GTCEu.LOGGER.error("Error loading dependency {} for model {}. Skipping...",
                            dependencyName, rl, e);
                }
            }
            ModelEventHelper.WRAPPED_MODELS.put(rl, shouldWrap);
            if (shouldWrap) {
                return new CTMBakedModel<>(baked);
            }

            return baked;
        });
    }
}
