package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final Set<AssetEventListener<?>> EVENT_LISTENERS = new ReferenceOpenHashSet<>();

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static void registerAtlasStitchedEventListener(AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add(listener);
    }

    public static void registerAtlasStitchedEventListener(final ResourceLocation atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add((AssetEventListener.AtlasStitched) event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    public static void registerBakeEventListener(AssetEventListener.ModifyBakingResult listener) {
        EVENT_LISTENERS.add(listener);
    }

    public static void registerAddModelsEventListener(AssetEventListener.RegisterAdditional listener) {
        EVENT_LISTENERS.add(listener);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<TextureStitchEvent.Post>) listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.ModifyBakingResult>) listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.RegisterAdditional>) listener).accept(event);
            }
        }
    }
}
