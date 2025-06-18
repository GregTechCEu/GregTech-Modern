package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gregtechceu.gtceu.client.model.machine.IPartModelRenderer;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("removal")
public class OverlayTieredMachineModel extends TieredHullMachineModel implements IPartModelRenderer {

    protected IModelRenderer overlayModel;

    public OverlayTieredMachineModel(int tier, ResourceLocation overlayModel) {
        super(tier, GTCEu.id("block/machine/template/hull_machine"));
        this.overlayModel = new IModelRenderer(overlayModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        // expand the overlay quads ever so slightly to combat z-fighting.
        overlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), quadFace, rand)
                .forEach(quad -> quads.add(ModelUtils.offsetQuad(quad, overlayQuadsOffset())));
    }

    public float overlayQuadsOffset() {
        return 0.004f;
    }
}
