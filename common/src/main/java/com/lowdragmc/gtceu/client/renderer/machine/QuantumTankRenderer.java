package com.lowdragmc.gtceu.client.renderer.machine;


import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.common.machine.storage.QuantumTankMachine;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote QuantumChestRenderer
 */
public class QuantumTankRenderer extends TieredHullMachineRenderer {

    public QuantumTankRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/quantum_tank"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMetaMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() instanceof QuantumTankMachine machine) {
            var stored = machine.getStored();
            if (!stored.isEmpty()) {
                var frontFacing = machine.getFrontFacing();
                var fluidTexture = FluidHelper.getStillTexture(stored);

                poseStack.pushPose();
                VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());
                RenderBufferUtils.renderCubeFace(poseStack, builder, 2.5f / 16, 2.5f / 16, 2.5f / 16, 13.5f / 16, 13.5f / 16, 13.5f / 16, FluidHelper.getColor(stored) | 0xff000000, 0xf000f0, fluidTexture);
                poseStack.popPose();

                poseStack.pushPose();
                RenderSystem.disableDepthTest();
                poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f, frontFacing.getStepZ() * -1 / 16f);
                RenderUtils.moveToFace(poseStack, 0, 0, 0, frontFacing);
                if (frontFacing.getAxis() == Direction.Axis.Y) {
                    RenderUtils.rotateToFace(poseStack, frontFacing, Direction.SOUTH);
                } else {
                    RenderUtils.rotateToFace(poseStack, frontFacing, null);
                }
                var amount = TextFormattingUtil.formatLongToCompactString(stored.getAmount(), 4);
                poseStack.scale(1f / 64, 1f / 64, 0);
                poseStack.translate(-32, -32, 0);
                new TextTexture(amount).draw(poseStack, 0, 0, 0, 24, 64, 28);
                RenderSystem.enableDepthTest();
                poseStack.popPose();
            }
        }
    }
}
