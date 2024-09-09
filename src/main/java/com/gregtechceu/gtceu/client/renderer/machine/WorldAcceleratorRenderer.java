package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;
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

public class WorldAcceleratorRenderer extends TieredHullMachineRenderer {

    private final WorkableOverlayModel blockEntityModeModel, randomTickModeModel;

    public WorldAcceleratorRenderer(int tier, ResourceLocation beModeModelPath, ResourceLocation rtModeModelPath) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        blockEntityModeModel = new WorkableOverlayModel(beModeModelPath);
        randomTickModeModel = new WorkableOverlayModel(rtModeModelPath);
    }

    private WorkableOverlayModel getModeModel(boolean isRandomTickMode) {
        if (isRandomTickMode) {
            return randomTickModeModel;
        }
        return blockEntityModeModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState, modelData,
                renderType);
        if (machine instanceof WorldAcceleratorMachine worldAcceleratorMachine) {
            WorkableOverlayModel model = getModeModel(worldAcceleratorMachine.isRandomTickMode());
            quads.addAll(model.bakeQuads(side, frontFacing, Direction.NORTH, worldAcceleratorMachine.isActive(),
                    worldAcceleratorMachine.isWorkingEnabled()));
        } else {
            quads.addAll(getModeModel(true).bakeQuads(side, frontFacing, Direction.NORTH, false, false));
        }
    }
}
