package com.gregtechceu.gtceu.client.util;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.Event;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AssetEventListener<T extends Event> {

    void accept(T event);

    default @Nullable Class<T> eventClass() {
        return null;
    }

    interface AtlasStitched extends AssetEventListener<TextureStitchEvent.Post> {

        @Override
        @Nullable
        default Class<TextureStitchEvent.Post> eventClass() {
            return TextureStitchEvent.Post.class;
        }
    }

    interface ModifyBakingResult extends AssetEventListener<ModelEvent.ModifyBakingResult> {

        @Override
        @Nullable
        default Class<ModelEvent.ModifyBakingResult> eventClass() {
            return ModelEvent.ModifyBakingResult.class;
        }
    }

    interface RegisterAdditional extends AssetEventListener<ModelEvent.RegisterAdditional> {

        @Override
        @Nullable
        default Class<ModelEvent.RegisterAdditional> eventClass() {
            return ModelEvent.RegisterAdditional.class;
        }
    }
}
