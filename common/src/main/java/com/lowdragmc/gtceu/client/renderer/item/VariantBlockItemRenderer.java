package com.lowdragmc.gtceu.client.renderer.item;

import com.lowdragmc.gtceu.api.item.VariantBlockItem;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote VariantBlockItemRenderer
 */
public class VariantBlockItemRenderer implements IRenderer {

    public final static VariantBlockItemRenderer INSTANCE = new VariantBlockItemRenderer();

    protected VariantBlockItemRenderer() {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack,
                            ItemTransforms.TransformType transformType,
                            boolean leftHand, PoseStack matrixStack,
                            MultiBufferSource buffer, int combinedLight,
                            int combinedOverlay, BakedModel model) {
        if (stack.getItem() instanceof VariantBlockItem<?,?> variantBlockItem) {
            IItemRendererProvider.disabled.set(true);
            var state = variantBlockItem.getBlockState(stack);
            model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay, model);
            IItemRendererProvider.disabled.set(false);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return true;
    }
}
