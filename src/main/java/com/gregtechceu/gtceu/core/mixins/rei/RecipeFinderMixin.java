package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeFinder.class, remap = false)
public class RecipeFinderMixin {

    @WrapOperation(method = "addNormalItem",
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
