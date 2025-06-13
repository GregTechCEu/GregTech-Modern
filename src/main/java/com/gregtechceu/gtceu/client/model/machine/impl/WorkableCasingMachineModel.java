package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.model.machine.WorkableOverlayModel;

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
import java.util.Map;

public class WorkableCasingMachineModel extends MachineModel {

    protected final WorkableOverlayModel overlayModel;
    protected final ResourceLocation baseCasing;

    public WorkableCasingMachineModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        this(baseCasing, workableModel, true);
    }

    public WorkableCasingMachineModel(ResourceLocation baseCasing, ResourceLocation workableModel, boolean tint) {
        super(tint ? GTCEu.id("block/cube/tinted/all") : GTCEu.id("block/cube/all"));
        this.overlayModel = new WorkableOverlayModel(workableModel);
        this.baseCasing = baseCasing;
        setTextureOverride(Map.of("all", baseCasing));
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        super.renderMachine(quads, definition, machine, frontFacing, quadFace, rand,
                modelFacing, modelState, modelData, renderType);
        if (machine instanceof IWorkable workable) {
            overlayModel.bakeQuads(quadFace, modelState, workable.isActive(), workable.isWorkingEnabled())
                    .forEach(quad -> quads.add(offsetQuad(quad, reBakeOverlayQuadsOffset())));
        } else {
            overlayModel.bakeQuads(quadFace, modelState, false, false)
                    .forEach(quad -> quads.add(offsetQuad(quad, reBakeOverlayQuadsOffset())));
        }
    }

    // @Override
    // @OnlyIn(Dist.CLIENT)
    // public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
    // super.onPrepareTextureAtlas(atlasName, register);
    // if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
    // overlayModel.registerTextureAtlas(register);
    // }
    // }

    public float reBakeOverlayQuadsOffset() {
        return 0.004f;
    }
}
