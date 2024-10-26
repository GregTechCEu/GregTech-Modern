package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.client.renderer.machine.OverlayEnergyIORenderer.*;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerRenderer
 */
public class BatteryBufferRenderer extends TieredHullMachineRenderer {

    private final int inventorySize;

    public BatteryBufferRenderer(int tier, int inventorySize) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.inventorySize = inventorySize;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side == frontFacing && modelFacing != null) {
            var texture = inventorySize <= 4 ? ENERGY_OUT_4A :
                    inventorySize <= 8 ? ENERGY_OUT_8A :
                            ENERGY_OUT_16A;
            texture.renderOverlay(quads, modelFacing, modelState, 2);
        }
    }
}
