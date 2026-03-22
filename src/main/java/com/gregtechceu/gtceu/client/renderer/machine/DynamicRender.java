package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.client.model.machine.IMachineRendererModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public abstract class DynamicRender<T extends IMachineFeature, S extends DynamicRender<T, S>>
                                   implements Comparable<DynamicRender<T, S>>, IMachineRendererModel<T> {

    public static final Codec<DynamicRender<?, ?>> CODEC = DynamicRenderManager.TYPE_CODEC
            .dispatchStable(DynamicRender::getType, DynamicRenderType::codec);

    @Getter
    @Setter
    protected MachineModel parent;

    public DynamicRender() {}

    public abstract DynamicRenderType<T, S> getType();

    @Override
    public MachineDefinition getDefinition() {
        return parent.getDefinition();
    }

    @Override
    public int compareTo(@NotNull DynamicRender<T, S> o) {
        return this.getType().compareTo(o.getType());
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return true;
    }
}
