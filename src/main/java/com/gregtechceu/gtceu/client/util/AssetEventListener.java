package com.gregtechceu.gtceu.client.util;

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

    interface AtlasStitched extends AssetEventListener<TextureAtlasStitchedEvent> {

        @Override
        @Nullable
        default Class<TextureAtlasStitchedEvent> eventClass() {
            return TextureAtlasStitchedEvent.class;
        }
    }

    interface ModifyBakingResult extends AssetEventListener<ModelEvent.ModifyBakingResult> {

        @Override
        @Nullable
        default Class<ModelEvent.ModifyBakingResult> eventClass() {
            return ModelEvent.ModifyBakingResult.class;
        }
    }
}
