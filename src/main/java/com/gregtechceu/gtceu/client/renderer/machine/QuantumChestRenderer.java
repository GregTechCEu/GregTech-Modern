package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import org.joml.Quaternionf;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote QuantumChestRenderer
 */
public class QuantumChestRenderer extends TieredHullMachineRenderer {

    private static Item CREATIVE_CHEST_ITEM = null;

    public QuantumChestRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/quantum_chest"));
    }

    public QuantumChestRenderer(int tier, ResourceLocation modelLocation) {
        super(tier, modelLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        if (CREATIVE_CHEST_ITEM == null) CREATIVE_CHEST_ITEM = GTMachines.CREATIVE_ITEM.getItem();
        model = getItemBakedModel();
        if (model != null && stack.hasTag()) {
            poseStack.pushPose();
            model.getTransforms().getTransform(transformType).apply(leftHand, poseStack);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            float tick = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
            // Don't need to handle locked items here since they don't get saved to the item
            renderChest(poseStack, buffer, Direction.NORTH, itemStack, storedAmount, tick, ItemStack.EMPTY,
                    stack.is(CREATIVE_CHEST_ITEM));

            poseStack.popPose();
        }
        super.renderItem(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof QuantumChestMachine machine) {
            var level = machine.getLevel();
            var frontFacing = machine.getFrontFacing();
            float tick = level.getGameTime() + partialTicks;
            renderChest(poseStack, buffer, frontFacing, machine.getStored(), machine.getStoredAmount(), tick,
                    machine.getLockedItem(), machine instanceof CreativeChestMachine);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderChest(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing, ItemStack stored,
                            long storedAmount,
                            float tick, ItemStack locked, boolean isCreative) {
        ItemStack itemStack = !stored.isEmpty() ? stored : locked;
        if (itemStack.isEmpty()) return;

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        poseStack.pushPose();
        BakedModel bakedmodel = itemRenderer.getModel(itemStack, Minecraft.getInstance().level, null,
                Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
        poseStack.translate(0.5D, 0.5d, 0.5D);
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1, 0));
        poseStack.scale(0.6f, 0.6f, 0.6f);
        itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, poseStack, buffer, 0xf000f0,
                OverlayTexture.NO_OVERLAY, bakedmodel);
        poseStack.popPose();

        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f,
                frontFacing.getStepZ() * -1 / 16f);
        RenderUtils.moveToFace(poseStack, 0, 0, 0, frontFacing);
        if (frontFacing.getAxis() == Direction.Axis.Y) {
            RenderUtils.rotateToFace(poseStack, frontFacing,
                    frontFacing == Direction.UP ? Direction.SOUTH : Direction.NORTH);
        } else {
            RenderUtils.rotateToFace(poseStack, frontFacing, null);
        }
        poseStack.scale(1f / 64, 1f / 64, 0);
        poseStack.translate(-32, -32, 0);

        TransformTexture text;
        if (isCreative) {
            text = new TextTexture("∞").setDropShadow(false).scale(3.0f);
        } else {
            var amount = stored.isEmpty() ? "*" : FormattingUtil.formatNumberReadable(storedAmount, false);
            text = new TextTexture(amount).setDropShadow(false);
        }
        text.draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack,
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())),
                0, 0, 0, 24, 64, 28);
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public float reBakeCustomQuadsOffset() {
        return 0f;
    }
}
