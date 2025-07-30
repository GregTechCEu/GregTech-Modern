package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.rei.impl.common.transfer.InputSlotCrafter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InputSlotCrafter.class, remap = false)
public class InputSlotCrafterMixin {

    @WrapOperation(method = "areItemsEqual",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
                            remap = true))
    private static boolean gtceu$ignoreGTToolNbt(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool && other.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(stack, other);
        }
        return original.call(stack, other);
    }

    @WrapOperation(method = "takeInventoryStack",
                   at = {
                           @At(value = "INVOKE",
                               target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z",
                               remap = true),
                           @At(value = "INVOKE",
                               target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z",
                               remap = true),
                   })
    private boolean gtceu$ignoreGTToolDamageAndEnchants(ItemStack stack, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(stack);
    }
}
