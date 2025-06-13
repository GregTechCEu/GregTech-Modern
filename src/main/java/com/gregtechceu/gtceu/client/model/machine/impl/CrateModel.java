package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

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

public class CrateModel extends MachineModel {

    private static final ResourceLocation MAINTENANCE_OVERLAY_TAPED = GTCEu
            .id("block/overlay/machine/overlay_crate_taped");

    public CrateModel(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        if (machine instanceof CrateMachine crate && crate.isTaped()) {
            for (var direction : GTUtil.DIRECTIONS) {
                quads.add(StaticFaceBakery.bakeFace(
                        StaticFaceBakery.SLIGHTLY_OVER_BLOCK, direction,
                        ModelFactory.getBlockSprite(MAINTENANCE_OVERLAY_TAPED), modelState, -1, 0, true, true));
            }
        }
    }
}
