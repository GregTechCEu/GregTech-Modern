package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.screen.ModularContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModularContainerMenu.class)
public abstract class ModularContainerMenuMixin {

    @Redirect(method = "clicked",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$mergeSpoilageBeforeSlotStackingMUI(ItemStack stack, ItemStack other) {
        SpoilUtils.mergeSpoilage(stack, other);
        return ItemStack.isSameItemSameTags(stack, other);
    }
}
