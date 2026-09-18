package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.neoforged.bus.api.Event;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AssetEventListener<T extends Event> {

    void accept(T event);

    default @Nullable Class<T> eventClass() {
        return null;
    }

    @FunctionalInterface
    interface AtlasStitched extends AssetEventListener<TextureStitchEvent.Post> {

        @Override
        @Nullable
        default Class<TextureStitchEvent.Post> eventClass() {
            return TextureStitchEvent.Post.class;
        }
    }

    @FunctionalInterface
    interface BakedModelReplacement {

        BakedModel modifyBakedModel(Identifier modelLocation, BakedModel model,
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
