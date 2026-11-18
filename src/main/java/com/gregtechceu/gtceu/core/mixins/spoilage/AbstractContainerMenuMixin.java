package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"),
            method = "canItemQuickReplace")
    private static void gtceu$mergeSpoilageBeforeSlotComparison(Slot slot, ItemStack stack, boolean stackSizeMatters,
                                                                CallbackInfoReturnable<Boolean> cir) {
        SpoilUtils.mergeSpoilage(stack, slot.getItem());
    }

    @Inject(at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"),
            method = "moveItemStackTo")
    private void gtceu$mergeSpoilageBeforeMove(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                               CallbackInfoReturnable<Boolean> cir,
                                               @Local(ordinal = 1) ItemStack itemstack) {
        SpoilUtils.mergeSpoilage(stack, itemstack);
    }

    @Redirect(method = "doClick",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$mergeSpoilageBeforeSlotStacking(ItemStack stack, ItemStack other) {
        SpoilUtils.mergeSpoilage(stack, other);
        return ItemStack.isSameItemSameTags(stack, other);
    }
}
