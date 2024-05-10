package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.client.util.ToolChargeBarRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
        method = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void gtceu$renderCustomDurabilityBars(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci, @Local PoseStack poseStack) {
        if (stack.getItem() instanceof IGTTool toolItem) {
            ToolChargeBarRenderer.renderBarsTool(poseStack, toolItem, stack, x, y);
        } else if (stack.getItem() instanceof IComponentItem componentItem) {
            ToolChargeBarRenderer.renderBarsItem(poseStack, componentItem, stack, x, y);
        }
    }
}
