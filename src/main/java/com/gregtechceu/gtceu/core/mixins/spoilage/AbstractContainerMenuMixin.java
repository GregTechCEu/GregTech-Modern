package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @WrapOperation(method = "canItemQuickReplace",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean gtceu$mergeSpoilageBeforeSlotComparison(ItemStack stack, ItemStack other,
                                                                   Operation<Boolean> original) {
        SpoilUtils.mergeSpoilables(stack, other);
        return original.call(stack, other);
    }

    @WrapOperation(method = "moveItemStackTo",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$mergeSpoilageBeforeMove(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        SpoilUtils.mergeSpoilables(stack, other);
        return original.call(stack, other);
    }

    @WrapOperation(method = "doClick",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$mergeSpoilageBeforeSlotStacking(ItemStack stack, ItemStack other,
                                                          Operation<Boolean> original) {
        SpoilUtils.mergeSpoilables(stack, other);
        return original.call(stack, other);
    }
}
