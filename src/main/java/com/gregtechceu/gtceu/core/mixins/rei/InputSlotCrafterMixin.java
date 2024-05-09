package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.api.items.IGTTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.rei.impl.common.transfer.InputSlotCrafter;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InputSlotCrafter.class, remap = false)
public class InputSlotCrafterMixin {
    @WrapOperation(method = "areItemsEqual", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", remap = true))
    private static boolean gtceu$modifyFindSlotMatcherREI(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(stack, other);
        }
        return original.call(stack, other);
    }

    @WrapOperation(method = "takeInventoryStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z", remap = true))
    private boolean gtceu$damagedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "takeInventoryStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z", remap = true))
    private boolean gtceu$enchantedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }
}
