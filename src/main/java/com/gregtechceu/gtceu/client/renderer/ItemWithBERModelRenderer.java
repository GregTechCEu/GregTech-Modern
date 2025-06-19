package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class ItemWithBERModelRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ItemWithBERModelRenderer INSTANCE = new ItemWithBERModelRenderer();

    protected final ItemRenderer itemRenderer;

    protected ItemWithBERModelRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext,
                             @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        BakedModel model = itemRenderer.getModel(stack, level, player, 0);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            berModel.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else {
            super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
        // also render the normal model here
        // because MC skips it if the model is a custom renderer
        boolean fabulous = true;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson() &&
                stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        }

        for (var renderPass : model.getRenderPasses(stack, fabulous)) {
            for (var renderType : renderPass.getRenderTypes(stack, fabulous)) {
                VertexConsumer consumer;
                if (fabulous) {
                    consumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
                } else {
                    consumer = ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil());
                }

                itemRenderer.renderModelLists(renderPass, stack, packedLight, packedOverlay, poseStack, consumer);
            }
        }
    }
}
