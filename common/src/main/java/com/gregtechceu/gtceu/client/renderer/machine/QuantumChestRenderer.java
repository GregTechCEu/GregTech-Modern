package com.gregtechceu.gtceu.client.renderer.machine;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote QuantumChestRenderer
 */
public class QuantumChestRenderer extends TieredHullMachineRenderer {

    public QuantumChestRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/quantum_chest"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMetaMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() instanceof QuantumChestMachine machine) {
            var itemStack = machine.getStored();
            if (!itemStack.isEmpty()) {
                var level = machine.getLevel();
                var frontFacing = machine.getFrontFacing();
                var itemRenderer = Minecraft.getInstance().getItemRenderer();
                float tick = level.getGameTime() + partialTicks;
                poseStack.pushPose();
                BakedModel bakedmodel = itemRenderer.getModel(itemStack, blockEntity.getLevel(), null, Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
                poseStack.translate(0.5D, 0.5d, 0.5D);
                poseStack.mulPose(Vector3f.YP.rotation(tick * Mth.TWO_PI / 80));
                poseStack.scale(0.6f, 0.6f, 0.6f);
                itemRenderer.render(itemStack, ItemTransforms.TransformType.FIXED, false, poseStack, buffer, 0xf000f0, OverlayTexture.NO_OVERLAY, bakedmodel);
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
                var amount = TextFormattingUtil.formatLongToCompactString(machine.getStoredAmount(), 4);
                poseStack.scale(1f / 64, 1f / 64, 0);
                poseStack.translate(-32, -32, 0);
                new TextTexture(amount).draw(poseStack, 0, 0, 0, 24, 64, 28);
                RenderSystem.enableDepthTest();
                poseStack.popPose();
            }
        }
    }
}
