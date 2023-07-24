package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ProcessingArrayMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/24
 * @implNote ProcessingArrayMachineRenderer
 */
public class ProcessingArrayMachineRenderer extends WorkableCasingMachineRenderer{
    public ProcessingArrayMachineRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel, false);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        // render held machine in the center
        if (machine instanceof ProcessingArrayMachine processingArray && processingArray.isFormed()) {
            var heldDefinition = processingArray.getMachineDefinition();
            if (heldDefinition != null && heldDefinition.getRenderer() instanceof MachineRenderer machineRenderer) {
                List<BakedQuad> machineQuad = new ArrayList<>();
                machineRenderer.renderMachine(machineQuad, definition, machine, frontFacing, side, rand, modelFacing, modelState);
                for (var quad : machineQuad) {
                    var bakedQuad = Quad.from(quad);
                    for (int i = 0; i < 4; i++) {
                        var pos = bakedQuad.getVert(i);
                        bakedQuad = bakedQuad.withVert(i, new Vector3f(pos.x() - frontFacing.getStepX(), pos.y() + 1, pos.z() - frontFacing.getStepZ()));
                    }
                    quads.add(bakedQuad.rebake());
                }
            }
        }
    }
}
