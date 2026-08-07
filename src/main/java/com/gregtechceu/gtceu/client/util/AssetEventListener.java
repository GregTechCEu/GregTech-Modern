package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AssetEventListener<T extends Event> {

    void accept(T event);

    default @Nullable Class<T> eventClass() {
        return null;
    }

    @FunctionalInterface
    interface AtlasStitched extends AssetEventListener<TextureAtlasStitchedEvent> {

        @Override
        @Nullable
        default Class<TextureAtlasStitchedEvent> eventClass() {
            return TextureAtlasStitchedEvent.class;
        }
    }

    @FunctionalInterface
    interface BakedModelReplacement {

        BakedModel modifyBakedModel(ModelResourceLocation modelLocation, BakedModel model,
                                    @Nullable UnbakedModel rootModel, ModelBakery modelBakery);
    }

    @FunctionalInterface
    interface RegisterAdditional extends AssetEventListener<ModelEvent.RegisterAdditional> {

        @Override
        @Nullable
        default Class<ModelEvent.RegisterAdditional> eventClass() {
            return ModelEvent.RegisterAdditional.class;
        }
    }
}
