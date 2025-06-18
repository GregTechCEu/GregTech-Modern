package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.machine.IMachineRendererModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DynamicRender<T extends MetaMachine, S extends DynamicRender<T, S>> extends BaseBakedModel
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData extraData, @Nullable RenderType renderType) {
        return List.of();
    }

    @Override
    public int compareTo(@NotNull DynamicRender<T, S> o) {
        return this.getType().compareTo(o.getType());
    }

    @Override
    public boolean isCustomRenderer() {
        return IMachineRendererModel.super.isCustomRenderer();
    }

}
