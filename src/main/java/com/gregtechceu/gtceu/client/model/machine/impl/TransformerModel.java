package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

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

import static com.gregtechceu.gtceu.client.model.machine.impl.OverlayEnergyIORenderer.*;

public class TransformerModel extends TieredHullMachineModel {

    private final int baseAmp;

    public TransformerModel(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.baseAmp = baseAmp;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        OverlayEnergyIORenderer otherFaceTexture = ENERGY_OUT_4A;
        OverlayEnergyIORenderer frontFaceTexture = ENERGY_IN_1A;
        var isTransformUp = false;
        if (machine instanceof TransformerMachine transformer) {
            isTransformUp = transformer.isTransformUp();
        }

        switch (baseAmp) {
            case 1 -> { // 1A <-> 4A
                otherFaceTexture = isTransformUp ? ENERGY_IN_4A : otherFaceTexture;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_1A : frontFaceTexture;
            }
            case 2 -> { // 2A <-> 8A
                otherFaceTexture = isTransformUp ? ENERGY_IN_8A : ENERGY_OUT_8A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_2A : ENERGY_IN_2A;
            }
            case 4 -> { // 4A <-> 16A
                otherFaceTexture = isTransformUp ? ENERGY_IN_16A : ENERGY_OUT_16A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_4A : ENERGY_IN_4A;
            }
            default -> { // 16A <-> 64A or more
                otherFaceTexture = isTransformUp ? ENERGY_IN_64A : ENERGY_OUT_64A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_16A : ENERGY_IN_16A;
            }
        }

        if (quadFace == frontFacing && modelFacing != null) {
            frontFaceTexture.renderOverlay(quads, modelFacing, modelState, 2);
        } else if (quadFace != null && modelFacing != null) {
            otherFaceTexture.renderOverlay(quads, modelFacing, modelState, 3);
        }
    }
}
