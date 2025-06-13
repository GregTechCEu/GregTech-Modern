package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

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

public class MaintenanceHatchPartModel extends OverlayTieredMachineModel {

    private static final ResourceLocation MAINTENANCE_OVERLAY_TAPED = GTCEu
            .id("block/overlay/machine/overlay_maintenance_taped");

    public MaintenanceHatchPartModel(int tier, ResourceLocation overlayModel) {
        super(tier, overlayModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand, modelFacing, modelState, modelData,
                renderType);
        if (quadFace == frontFacing && modelFacing != null && machine instanceof IMaintenanceMachine maintenanceHatch &&
                maintenanceHatch.isTaped()) {
            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY, modelFacing,
                    ModelFactory.getBlockSprite(MAINTENANCE_OVERLAY_TAPED), modelState, -1, 0, true, true));
        }
    }
}
