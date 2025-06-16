package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.client.model.machine.WorkableOverlays;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorkableSteamMachineModel extends SteamHullMachineModel {

    public static final ResourceLocation VENT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_steam_vent");
    protected final WorkableOverlays overlayModel;

    public WorkableSteamMachineModel(boolean isHighTier, ResourceLocation overlay) {
        super(isHighTier, GTCEu.id("block/machine/hull_machine"));
        this.overlayModel = new WorkableOverlays(overlay);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IWorkable workable) {
            quads.addAll(overlayModel.bakeQuads(side, modelState, workable.isActive(),
                    workable.isWorkingEnabled()));
        } else {
            quads.addAll(overlayModel.bakeQuads(side, modelState, false, false));
        }
        if (machine instanceof IExhaustVentMachine exhaustVentMachine) {
            if (side != null && exhaustVentMachine.getVentingDirection() == side && modelFacing != null) {
                quads.add(
                        StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(VENT_OVERLAY), modelState));
            }
        }
    }
}
