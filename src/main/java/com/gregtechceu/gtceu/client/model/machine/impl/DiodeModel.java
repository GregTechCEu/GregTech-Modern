package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay.*;

public class DiodeModel extends TieredHullMachineModel {

    public DiodeModel(int tier) {
        super(tier, GTCEu.id("block/machine/template/hull_machine"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        EnergyIOOverlay energyIn = ENERGY_IN_1A;
        EnergyIOOverlay energyOut = ENERGY_OUT_1A;
        var amps = 1;
        if (machine instanceof DiodePartMachine diode) {
            amps = diode.getAmps();
        }
        switch (amps) {
            case 2 -> {
                energyIn = ENERGY_IN_2A;
                energyOut = ENERGY_OUT_2A;
            }
            case 4 -> {
                energyIn = ENERGY_IN_4A;
                energyOut = ENERGY_OUT_4A;
            }
            case 8 -> {
                energyIn = ENERGY_IN_8A;
                energyOut = ENERGY_OUT_8A;
            }
            case 16 -> {
                energyIn = ENERGY_IN_16A;
                energyOut = ENERGY_OUT_16A;
            }
        }

        if (quadFace == frontFacing && modelFacing != null) {
            energyOut.renderOverlay(quads, modelFacing, modelState, 2);
        } else if (quadFace != null && modelFacing != null) {
            energyIn.renderOverlay(quads, modelFacing, modelState, 2);
        }
    }
}
