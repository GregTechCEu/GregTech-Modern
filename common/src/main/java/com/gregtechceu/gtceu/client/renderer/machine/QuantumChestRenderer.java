package com.gregtechceu.gtceu.client.renderer.machine;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        model = getItemBakedModel();
        if (model != null && stack.hasTag()) {
            poseStack.pushPose();
            model.getTransforms().getTransform(transformType).apply(leftHand, poseStack);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            int storedAmount = stack.getOrCreateTag().getInt("storedAmount");
            float tick = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
            renderChest(poseStack, buffer, Direction.NORTH, itemStack, storedAmount, tick);

            poseStack.popPose();
        }
        super.renderItem(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() instanceof QuantumChestMachine machine) {
            var level = machine.getLevel();
            var frontFacing = machine.getFrontFacing();
            float tick = level.getGameTime() + partialTicks;
            renderChest(poseStack, buffer, frontFacing, machine.getStored(), machine.getStoredAmount(), tick);
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderChest(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing, ItemStack itemStack, int storedAmount, float tick) {
        if (!itemStack.isEmpty()) {
            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.pushPose();
            BakedModel bakedmodel = itemRenderer.getModel(itemStack, Minecraft.getInstance().level, null, Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
            poseStack.translate(0.5D, 0.5d, 0.5D);
            poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1, 0));
            poseStack.scale(0.6f, 0.6f, 0.6f);
            itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, poseStack, buffer, 0xf000f0, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();


            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f, frontFacing.getStepZ() * -1 / 16f);
            RenderUtils.moveToFace(poseStack, 0, 0, 0, frontFacing);
            if (frontFacing.getAxis() == Direction.Axis.Y) {
                RenderUtils.rotateToFace(poseStack, frontFacing, frontFacing == Direction.UP ? Direction.SOUTH : Direction.NORTH);
            } else {
                RenderUtils.rotateToFace(poseStack, frontFacing, null);
            }
            var amount = TextFormattingUtil.formatLongToCompactString(storedAmount, 4);
            poseStack.scale(1f / 64, 1f / 64, 0);
            poseStack.translate(-32, -32, 0);
            new TextTexture(amount).draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())), 0, 0, 0, 24, 64, 28);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    @Environment(EnvType.CLIENT)
    public float reBakeCustomQuadsOffset() {
        return 0f;
    }
}
