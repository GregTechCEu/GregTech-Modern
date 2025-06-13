package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.client.model.machine.impl.OverlayEnergyIORenderer.ENERGY_OUT_1A;

public class SimpleGeneratorMachineModel extends WorkableTieredHullMachineModel {

    public SimpleGeneratorMachineModel(int tier, ResourceLocation workableModel) {
        super(tier, workableModel);
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand, Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        if (quadFace == frontFacing && modelFacing != null) {
            ENERGY_OUT_1A.renderOverlay(quads, modelFacing, modelState, 2);
        }
    }
}
