package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public class InventoryMixin {
    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$modifyFindSlotMatcher(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(stack, other);
        }
        return original.call(stack, other);
    }

    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z", remap = true))
    private boolean gtceu$damagedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z", remap = true))
    private boolean gtceu$enchantedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }
}
