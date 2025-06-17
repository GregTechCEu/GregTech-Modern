package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.machine.IMachineRendererModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DynamicMachineRenderer<T extends MetaMachine> extends BaseBakedModel
        implements Comparable<DynamicMachineRenderer<T>>, IMachineRendererModel<T> {

    @Getter
    protected final MachineModel parent;
    @Getter
    private final DynamicMachineRendererType type;

    public DynamicMachineRenderer(DynamicMachineRendererType type, MachineModel parent) {
        this.type = type;
        this.parent = parent;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData extraData, @Nullable RenderType renderType) {
        return List.of();
    }

    @Override
    public int compareTo(@NotNull DynamicMachineRenderer<T> o) {
        return this.type.compareTo(o.type);
    }
}
