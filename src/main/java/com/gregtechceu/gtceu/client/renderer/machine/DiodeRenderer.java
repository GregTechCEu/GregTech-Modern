package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class DiodeRenderer extends TieredHullMachineRenderer{

    public DiodeRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = TransformerRenderer.ENERGY_IN_1A;
        var frontFaceTexture = TransformerRenderer.ENERGY_OUT_1A;
        var amps = 1;
        if (machine instanceof DiodePartMachine diode) {
            amps = diode.getAmps();
        }
        switch(amps) {
            case 2 -> {
                otherFaceTexture = TransformerRenderer.ENERGY_IN_2A;
                frontFaceTexture = TransformerRenderer.ENERGY_OUT_2A;
            }
            case 4 -> {
                otherFaceTexture = TransformerRenderer.ENERGY_IN_4A;
                frontFaceTexture = TransformerRenderer.ENERGY_OUT_4A;
            }
            case 8 ->  {
                otherFaceTexture = TransformerRenderer.ENERGY_IN_8A;
                frontFaceTexture = TransformerRenderer.ENERGY_OUT_8A;
            }
            case 16 ->  {
                otherFaceTexture = TransformerRenderer.ENERGY_IN_16A;
                frontFaceTexture = TransformerRenderer.ENERGY_OUT_16A;
            }
        }

        if (side == frontFacing && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(frontFaceTexture), modelState, 2));
        } else if (side != null && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(otherFaceTexture), modelState, 2));
        }

    }
}
