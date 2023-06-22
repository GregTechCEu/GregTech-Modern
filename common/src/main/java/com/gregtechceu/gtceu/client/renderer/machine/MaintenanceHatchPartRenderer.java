package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceHatchPartRenderer extends MachineRenderer {
    private static final IModelRenderer MAINTENANCE_OVERLAY = new IModelRenderer(GTCEu.id("overlay/machine/overlay_maintenance"));
    private static final IModelRenderer MAINTENANCE_OVERLAY_TAPED = new IModelRenderer(GTCEu.id("overlay/machine/overlay_maintenance_taped"));
    private static final IModelRenderer MAINTENANCE_OVERLAY_CONFIGURABLE = new IModelRenderer(GTCEu.id("overlay/machine/overlay_maintenance_configurable"));
    private static final IModelRenderer MAINTENANCE_OVERLAY_FULL_AUTO = new IModelRenderer(GTCEu.id("overlay/machine/overlay_maintenance_full_auto"));
    private static final IModelRenderer MAINTENANCE_OVERLAY_CLEANING = new IModelRenderer(GTCEu.id("overlay/machine/overlay_maintenance_cleaning"));

    public MaintenanceHatchPartRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof MaintenanceHatchPartMachine maintenanceHatch) {
            quads.addAll((maintenanceHatch.isConfigurable() ? MAINTENANCE_OVERLAY_CONFIGURABLE : maintenanceHatch.isTaped() ? MAINTENANCE_OVERLAY_TAPED : MAINTENANCE_OVERLAY)
                    .renderModel(machine.getLevel(), machine.getPos(), machine.getBlockState(), side, rand));
        }

    }
}
