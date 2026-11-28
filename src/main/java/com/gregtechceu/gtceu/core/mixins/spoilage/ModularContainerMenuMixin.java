package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.screen.ModularContainerMenu;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModularContainerMenu.class)
public abstract class ModularContainerMenuMixin {

    @WrapOperation(method = { "clicked", "transferItem" },
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$mergeSpoilageBeforeSlotStackingMUI(ItemStack stack, ItemStack other,
                                                             Operation<Boolean> original) {
        SpoilUtils.mergeSpoilables(stack, other);
        return original.call(stack, other);
    }
}
