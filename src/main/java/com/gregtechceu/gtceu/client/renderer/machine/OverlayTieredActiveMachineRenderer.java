package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

public class OverlayTieredActiveMachineRenderer extends TieredHullMachineRenderer {
    protected IModelRenderer activeOverlayModel;
    protected IModelRenderer overlayModel;

    public OverlayTieredActiveMachineRenderer(int tier, ResourceLocation overlayModel, ResourceLocation activeOverlayModel) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.overlayModel = new IModelRenderer(overlayModel);
        this.activeOverlayModel = new IModelRenderer(activeOverlayModel);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IRecipeLogicMachine rlm) {
            if (rlm.isActive()) {
                quads.addAll(activeOverlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand));
                return;
            }
        } else if (machine instanceof IMultiPart part) {
            if (part.getControllers().stream().anyMatch(controller -> controller instanceof IRecipeLogicMachine rlm && rlm.isActive())) {
                quads.addAll(activeOverlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand));
                return;
            }
        }
        quads.addAll(overlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand));
    }
}
