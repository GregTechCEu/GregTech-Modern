package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

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

import static com.gregtechceu.gtceu.client.model.machine.impl.OverlayEnergyIORenderer.*;

public class ConverterModel extends TieredHullMachineModel {

    private static final ResourceLocation CONVERTER_FE_IN = GTCEu.id("block/overlay/converter/converter_native_in");
    private static final ResourceLocation CONVERTER_FE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");
    private final OverlayEnergyIORenderer energyIn;
    private final OverlayEnergyIORenderer energyOut;

    public ConverterModel(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        switch (baseAmp) {
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
            default -> {
                energyIn = ENERGY_IN_1A;
                energyOut = ENERGY_OUT_1A;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand, Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        var isFeToEu = false;
        if (machine instanceof ConverterMachine converter) {
            isFeToEu = converter.isFeToEu();
        }
        if (quadFace == frontFacing && modelFacing != null) {
            if (isFeToEu) {
                energyOut.renderOverlay(quads, modelFacing, modelState, 2);
            } else {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(CONVERTER_FE_OUT),
                        modelState, -1));
            }
        } else if (quadFace != null && modelFacing != null) {
            if (isFeToEu) {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(CONVERTER_FE_IN),
                        modelState, -1));
            } else {
                energyIn.renderOverlay(quads, modelFacing, modelState, 2);
            }
        }
    }
}
