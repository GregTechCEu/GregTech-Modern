package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Priority increased to override Industrial Upgrade, see gtm#3763
@Mixin(value = Inventory.class, priority = 1200)
public abstract class InventoryMixin {

    @WrapOperation(method = { "findSlotMatchingUnusedItem", "findSlotMatchingItem" },
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$ignoreGTToolNbt(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool && other.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(stack, other);
        }
        return original.call(stack, other);
    }

    @WrapOperation(method = "findSlotMatchingUnusedItem",
                   at = {
                           @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z"),
                           @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"),
                   })
    private boolean gtceu$ignoreGTToolDamageAndEnchants(ItemStack stack, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(stack);
    }
}
