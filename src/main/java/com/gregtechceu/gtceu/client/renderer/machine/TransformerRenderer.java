package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.client.renderer.machine.OverlayEnergyIORenderer.*;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerRenderer
 */
public class TransformerRenderer extends TieredHullMachineRenderer {

    private final int baseAmp;

    public TransformerRenderer(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.baseAmp = baseAmp;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
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

        if (side == frontFacing && modelFacing != null) {
            frontFaceTexture.renderOverlay(quads, modelFacing, modelState, 2);
        } else if (side != null && modelFacing != null) {
            otherFaceTexture.renderOverlay(quads, modelFacing, modelState, 3);
        }
    }
}
