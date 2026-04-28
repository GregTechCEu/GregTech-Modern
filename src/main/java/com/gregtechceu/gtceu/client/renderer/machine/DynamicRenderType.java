package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public record DynamicRenderType<T extends IMachineFeature, S extends DynamicRender<T, S>>(MapCodec<S> codec)
        implements Comparable<DynamicRenderType<T, S>> {

    public Identifier getId() {
        return DynamicRenderManager.getId(this);
    }

    @Override
    public int compareTo(@NotNull DynamicRenderType<T, S> o) {
        return this.getId().compareTo(o.getId());
    }
}
