package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateRenderer extends MachineRenderer {

    private static final ResourceLocation MAINTENANCE_OVERLAY_TAPED = GTCEu
            .id("block/overlay/machine/overlay_crate_taped");

    public CrateRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof CrateMachine crate && crate.isTaped()) {
            for (var direction : GTUtil.DIRECTIONS) {
                quads.add(StaticFaceBakery.bakeFace(
                        StaticFaceBakery.SLIGHTLY_OVER_BLOCK, direction,
                        ModelFactory.getBlockSprite(MAINTENANCE_OVERLAY_TAPED), modelState, -1, 0, true, true));
            }
        }
    }
}
