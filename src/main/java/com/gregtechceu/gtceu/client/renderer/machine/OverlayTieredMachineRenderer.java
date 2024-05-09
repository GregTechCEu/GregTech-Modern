package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machines.MachineDefinition;
import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote PartRenderer
 */
public class OverlayTieredMachineRenderer extends TieredHullMachineRenderer implements IPartRenderer {
    protected IModelRenderer overlayModel;

    public OverlayTieredMachineRenderer(int tier, ResourceLocation overlayModel) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.overlayModel = new IModelRenderer(overlayModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        quads.addAll(overlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand));
    }
}
