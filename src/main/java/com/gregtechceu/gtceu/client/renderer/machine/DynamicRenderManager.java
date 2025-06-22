package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;

public final class DynamicRenderManager {

    public static final BinaryOperator<String> MODEL_ID_FORMATTER = "/block/machine/%s/dynamic_render/%s"::formatted;

    public static final Codec<DynamicRenderType<?, ?>> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(
            id -> {
                var type = DynamicRenderManager.getType(id);
                if (type != null) {
                    return DataResult.success(type);
                } else {
                    return DataResult.error(() -> "Dynamic render type with ID " + id + " does not exist");
                }
            }, type -> {
                ResourceLocation id = getId(type);
                if (id != null) {
                    return DataResult.success(id);
                } else {
                    return DataResult.error(() -> "Dynamic render type " + type + " is not registered");
                }
            });

    private static final BiMap<ResourceLocation, DynamicRenderType<?, ?>> DYNAMIC_RENDERER_TYPES = HashBiMap.create(5);

    // spotless:off
    public static <T extends IMachineFeature, S extends DynamicRender<T, S>> DynamicRenderType<T, S> register(ResourceLocation id,
                                                                                                              DynamicRenderType<T, S> type) {
        if (DYNAMIC_RENDERER_TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Cannot register multiple dynamic render types with the same id! Tried " + id);
        }
        DYNAMIC_RENDERER_TYPES.put(id, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IMachineFeature, S extends DynamicRender<T, S>> @Nullable DynamicRenderType<T, S> getType(ResourceLocation id) {
        return (DynamicRenderType<T, S>) DYNAMIC_RENDERER_TYPES.get(id);
    }

    public static @Nullable ResourceLocation getId(DynamicRenderType<?, ?> type) {
        return DYNAMIC_RENDERER_TYPES.inverse().get(type);
    }
    // spotless:on

    private DynamicRenderManager() {}
}
