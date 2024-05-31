package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;
import com.gregtechceu.gtceu.common.machine.electric.WorldAcceleratorMachine;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

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
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof WorldAcceleratorMachine worldAcceleratorMachine) {
            WorkableOverlayModel model = getModeModel(worldAcceleratorMachine.isRandomTickMode());
            quads.addAll(model.bakeQuads(side, frontFacing, Direction.NORTH, worldAcceleratorMachine.isActive(),
                    worldAcceleratorMachine.isWorkingEnabled()));
        } else {
            quads.addAll(getModeModel(true).bakeQuads(side, frontFacing, Direction.NORTH, false, false));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            randomTickModeModel.registerTextureAtlas(register);
            blockEntityModeModel.registerTextureAtlas(register);
        }
    }
}
