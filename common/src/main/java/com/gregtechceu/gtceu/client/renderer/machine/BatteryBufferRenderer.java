package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerRenderer
 */
public class BatteryBufferRenderer extends TieredHullMachineRenderer{
    private final int inventorySize;

    public BatteryBufferRenderer(int tier, int inventorySize) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.inventorySize = inventorySize;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side == frontFacing && modelFacing != null) {
            var texture = inventorySize <= 4 ? TransformerRenderer.ENERGY_OUT :
                    inventorySize <= 8 ? TransformerRenderer.ENERGY_OUT_MULTI :
                            TransformerRenderer.ENERGY_OUT_ULTRA;
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(texture), modelState, 2));
        }
    }

}
