package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.capability.impl.miner.MinerLogic;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeMinerRenderer extends WorkableCasingMachineRenderer {
    public static final AABB BEHIND_BLOCK = new AABB(0, -0.005, 1, 1, 1, 2);


    public LargeMinerRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @Override
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side == Direction.DOWN) quads.add(FaceQuad.bakeFace(BEHIND_BLOCK, modelFacing, ModelFactory.getBlockSprite(MinerRenderer.PIPE_IN_OVERLAY), modelState, -1, 15, true, true));
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, stack, buffer, combinedLight, combinedOverlay);
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            if (machineBlockEntity.getMetaMachine() instanceof IRecipeLogicMachine logicMachine && logicMachine.getRecipeLogic() instanceof MinerLogic minerLogic) {
                Direction facing = blockEntity.getBlockState().getValue(machineBlockEntity.getDefinition().get().getRotationState().property);

                stack.pushPose();
                stack.translate(-facing.getStepX(), 0, -facing.getStepZ());
                Direction modelFacing = blockEntity.getBlockState().getValue(machineBlockEntity.getDefinition().get().getRotationState().property);
                minerLogic.renderPipe(stack, buffer, modelFacing, combinedLight, combinedOverlay);
                stack.popPose();
            }
        }

    }
}
