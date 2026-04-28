package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.item.datacomponents.LargeItemContent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.LightTexture;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.PoseStackExtensions;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

@ExtensionMethod(PoseStackExtensions.class)
public class QuantumChestItemRender extends DynamicRender<QuantumChestMachine, QuantumChestItemRender> {

    // spotless:off
    public static final MapCodec<QuantumChestItemRender> CODEC = MapCodec.unit(QuantumChestItemRender::new);
    public static final DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> TYPE = new DynamicRenderType<>(QuantumChestItemRender.CODEC);
    // spotless:on

    private static @Nullable Item CREATIVE_CHEST_ITEM = null;

    public QuantumChestItemRender() {}

    @Override
    public DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> getType() {
        return TYPE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (CREATIVE_CHEST_ITEM == null) CREATIVE_CHEST_ITEM = GTMachines.CREATIVE_ITEM.getItem();

        LargeItemContent content = stack.get(GTDataComponents.LARGE_ITEM_CONTENT);
        if (content != null) {
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.5f, -0.5f);

            ItemStack itemStack = content.stored();
            long storedAmount = content.amount();
            float totalTick = Minecraft.getInstance().player.tickCount +
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            // Don't need to handle locked items here since they don't get saved to the item
            renderChestItem(poseStack, buffer, totalTick, Direction.NORTH,
                    itemStack, storedAmount, ItemStack.EMPTY, stack.is(CREATIVE_CHEST_ITEM));

            poseStack.popPose();
        }
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(QuantumChestMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        setupModelRotation(machine, poseStack);

        var totalTick = machine.getLevel().getGameTime() + partialTick;
        renderChestItem(poseStack, buffer, totalTick, machine.getFrontFacing(),
                machine.getStored(), machine.getStoredAmount(), machine.getLockedItem(),
                machine instanceof CreativeChestMachine);
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderChestItem(PoseStack poseStack, MultiBufferSource buffer, float totalTick, Direction frontFacing,
                                ItemStack stored, long storedAmount, ItemStack locked, boolean isCreative) {
        ItemStack itemStack = !stored.isEmpty() ? stored : locked;
        if (itemStack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (frontFacing.getAxis() == Direction.Axis.Y) {
            Quaternionf rotation = getRotation(Direction.NORTH, frontFacing);
            poseStack.mulPose(rotation);
        }
        poseStack.mulPose(new Quaternionf().rotateY(totalTick * Mth.TWO_PI / 80));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        renderItemStack(itemStack, poseStack, buffer,
                Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
        poseStack.popPose();

        drawAmountText(poseStack, buffer, frontFacing, storedAmount, isCreative);
    }

    private static void renderItemStack(ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int seed) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.FIXED,
                minecraft.level, null, seed);

        SubmitNodeStorage submitStorage = new SubmitNodeStorage();
        renderState.submit(poseStack, submitStorage, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        for (SubmitNodeCollection submits : submitStorage.getSubmitsPerOrder().values()) {
            for (SubmitNodeStorage.ItemSubmit submit : submits.getItemSubmits()) {
                renderSubmittedItem(buffer, submit);
            }
        }
    }

    private static void renderSubmittedItem(MultiBufferSource buffer, SubmitNodeStorage.ItemSubmit submit) {
        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setLightCoords(submit.lightCoords());
        quadInstance.setOverlayCoords(submit.overlayCoords());

        for (BakedQuad quad : submit.quads()) {
            var material = quad.materialInfo();
            var renderType = material.itemRenderType();
            quadInstance.setColor(getLayerColorSafe(submit.tintLayers(), material));

            if (submit.foilType() != ItemStackRenderState.FoilType.NONE) {
                ItemFeatureRenderer.getFoilBuffer(buffer, renderType, true, true)
                        .putBakedQuad(submit.pose(), quad, quadInstance);
            }

            buffer.getBuffer(renderType).putBakedQuad(submit.pose(), quad, quadInstance);
        }
    }

    private static int getLayerColorSafe(int[] tintLayers, BakedQuad.MaterialInfo material) {
        return material.isTinted() && material.tintIndex() >= 0 && material.tintIndex() < tintLayers.length ?
                tintLayers[material.tintIndex()] : -1;
    }

    public static void setupModelRotation(MetaMachine machine, PoseStack poseStack) {
        var frontFacing = machine.getFrontFacing();
        var upwardFacing = machine.getUpwardsFacing();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        rotateMatrix(poseStack.last().pose(),
                upwardFacingAngle(upwardFacing) + (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0),
                getDirectionAxis(frontFacing));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void drawAmountText(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing,
                                      long storedAmount, boolean isCreative) {
        poseStack.pushPose();
        poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f,
                frontFacing.getStepZ() * -1 / 16f);

        RenderUtil.moveToFace(poseStack, 0.5f, 0.5f, 0.5f, frontFacing);
        RenderUtil.rotateToFace(poseStack, frontFacing, Direction.NORTH);
        poseStack.scale(1f / 64, 1f / 64, 0);
        poseStack.translate(-32, -32, 0);

        String text;
        int x = 0, y = 24;
        int w = 64, h = 28;
        float textX = x + w / 2.0f;
        float textY = y + h / 2.0f;

        poseStack.pushPose();
        if (isCreative) {
            text = "∞";
            poseStack.translate(textX, textY, 0);
            poseStack.scale(3.0f, 3.0f, 1.0f);
            poseStack.translate(-textX, -textY, 0);
        } else {
            text = storedAmount <= 0 ? "*" : FormattingUtil.formatNumberReadable(storedAmount, false);
        }

        Font font = Minecraft.getInstance().font;
        font.drawInBatch(text, textX - font.getSplitter().stringWidth(text) / 2.0f, textY - font.lineHeight / 2.0f,
                0xffffffff, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
        poseStack.popPose();
    }
}
