package com.gregtechceu.gtceu.client.renderer.machine;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public final class DynamicMachineRendererRegistry {

    private static final BiMap<ResourceLocation, DynamicMachineRendererType> DYNAMIC_RENDERER_TYPES = HashBiMap.create(5);

    public static DynamicMachineRendererType register(ResourceLocation id, DynamicMachineRendererType type) {
        DYNAMIC_RENDERER_TYPES.put(id, type);
        return type;
    }

    public static DynamicMachineRendererType getType(ResourceLocation id) {
        return DYNAMIC_RENDERER_TYPES.get(id);
    }

    public static ResourceLocation getId(DynamicMachineRendererType type) {
        return DYNAMIC_RENDERER_TYPES.inverse().get(type);
    }

    private DynamicMachineRendererRegistry() {}
}
