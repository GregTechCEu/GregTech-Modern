package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IControllerModelRenderer {

    /**
     * Render a specific model for given part.
     */
    @OnlyIn(Dist.CLIENT)
    void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part,
                         Direction frontFacing, @Nullable Direction side, RandomSource rand,
                         @NotNull ModelData modelData, @Nullable RenderType renderType);
}
