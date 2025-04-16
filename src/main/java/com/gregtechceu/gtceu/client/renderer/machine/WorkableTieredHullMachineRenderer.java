package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;

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

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote WorkableTieredHullMachineRenderer
 */
public class WorkableTieredHullMachineRenderer extends TieredHullMachineRenderer {

    protected final WorkableOverlayModel overlayModel;

    public WorkableTieredHullMachineRenderer(int tier, ResourceLocation workableModel) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.overlayModel = new WorkableOverlayModel(workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IWorkable workable) {
            overlayModel.bakeQuads(side, modelState, workable.isActive(), workable.isWorkingEnabled())
                    .forEach(quad -> quads.add(Quad.from(quad, overlayQuadsOffset()).rebake()));
        } else {
            overlayModel.bakeQuads(side, modelState, false, false)
                    .forEach(quad -> quads.add(Quad.from(quad, overlayQuadsOffset()).rebake()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            overlayModel.registerTextureAtlas(register);
        }
    }

    public float overlayQuadsOffset() {
        return 0.002f;
    }
}
