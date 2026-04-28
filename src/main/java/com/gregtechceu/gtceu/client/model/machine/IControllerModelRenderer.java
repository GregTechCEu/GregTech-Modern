package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IControllerModelRenderer {

    /**
     * Render a specific model for given part.
     */
    @OnlyIn(Dist.CLIENT)
    void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine machine, IMultiPart part,
                         Direction frontFacing, @Nullable Direction side, RandomSource rand,
                         @NotNull ModelData modelData, @Nullable RenderType renderType);
}
