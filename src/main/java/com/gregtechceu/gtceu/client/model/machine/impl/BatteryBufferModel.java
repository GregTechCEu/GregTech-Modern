package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

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

public class BatteryBufferModel extends TieredHullMachineModel {

    private final int inventorySize;

    public BatteryBufferModel(int tier, int inventorySize) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.inventorySize = inventorySize;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand, Direction modelFacing,
                              ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        if (quadFace == frontFacing && modelFacing != null) {
            var texture = inventorySize <= 4 ? ENERGY_OUT_4A :
                    inventorySize <= 8 ? ENERGY_OUT_8A :
                            ENERGY_OUT_16A;
            texture.renderOverlay(quads, modelFacing, modelState, 2);
        }
    }
}
