package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.machine.electric.WorldAcceleratorMachine;

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

public class WorldAcceleratorModel extends TieredHullMachineModel {

    private final WorkableOverlays blockEntityModeModel, randomTickModeModel;

    public WorldAcceleratorModel(int tier, ResourceLocation beModeModelPath, ResourceLocation rtModeModelPath) {
        super(tier, GTCEu.id("block/machine/template/hull_machine"));
        blockEntityModeModel = new WorkableOverlays(beModeModelPath);
        randomTickModeModel = new WorkableOverlays(rtModeModelPath);
    }

    private WorkableOverlays getModeModel(boolean isRandomTickMode) {
        if (isRandomTickMode) {
            return randomTickModeModel;
        }
        return blockEntityModeModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        if (machine instanceof WorldAcceleratorMachine worldAcceleratorMachine) {
            WorkableOverlays model = getModeModel(worldAcceleratorMachine.isRandomTickMode());
            quads.addAll(model.bakeQuads(quadFace, modelState, worldAcceleratorMachine.isActive(),
                    worldAcceleratorMachine.isWorkingEnabled()));
        } else {
            quads.addAll(getModeModel(true).bakeQuads(quadFace, modelState, false, false));
        }
    }
}
