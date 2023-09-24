package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComponentPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAEmptyPartMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class HPCAPartRenderer extends TieredHullMachineRenderer {

    private final ResourceLocation baseOverlay, damagedOverlay, activeOverlay, activeDamagedOverlay;

    public HPCAPartRenderer(ResourceLocation baseOverlay, ResourceLocation damagedOverlay, ResourceLocation activeOverlay, ResourceLocation activeDamagedOverlay) {
        super(GTValues.ZPM, GTCEu.id("block/machine/hull_machine"));
        this.baseOverlay = baseOverlay;
        this.damagedOverlay = damagedOverlay;
        this.activeOverlay = activeOverlay;
        this.activeDamagedOverlay = activeDamagedOverlay;
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side == frontFacing && modelFacing != null && machine instanceof HPCAComponentPartMachine part) {
            if (damagedOverlay != null && part.isDamaged()) {
                if (activeDamagedOverlay != null && part.getControllers().stream().anyMatch(controller -> controller instanceof IRecipeLogicMachine recipe && recipe.isActive())) {
                    quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(activeDamagedOverlay), modelState));
                } else {
                    quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(damagedOverlay), modelState));
                }
            } else if (activeOverlay != null && part.getControllers().stream().anyMatch(controller -> controller instanceof IRecipeLogicMachine recipe && recipe.isActive())) {
                quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(activeOverlay), modelState));
            } else {
                quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(baseOverlay), modelState));
            }
        }
    }

    @Override
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(InventoryMenu.BLOCK_ATLAS)) {
            if (baseOverlay != null) register.accept(baseOverlay);
            if (damagedOverlay != null) register.accept(damagedOverlay);
            if (activeOverlay != null) register.accept(activeOverlay);
            if (activeDamagedOverlay != null) register.accept(activeDamagedOverlay);
        }
    }
}
