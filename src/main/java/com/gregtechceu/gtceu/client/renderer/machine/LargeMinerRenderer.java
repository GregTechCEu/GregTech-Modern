package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machines.MachineDefinition;
import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.gregtechceu.gtceu.api.machines.feature.multiblock.IMultiController;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeMinerRenderer extends WorkableCasingMachineRenderer {
    public static final AABB BEHIND_BLOCK = new AABB(0, -0.005, 1, 1, 1, 2);

    public LargeMinerRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IMultiController controller && controller.isFormed() && side == Direction.DOWN && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(BEHIND_BLOCK, modelFacing, ModelFactory.getBlockSprite(MinerRenderer.PIPE_IN_OVERLAY), modelState, -101, 15, true, true));
        }
    }

}
