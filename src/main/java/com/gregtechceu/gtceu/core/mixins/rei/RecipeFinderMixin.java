package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeFinder.class, remap = false)
public class RecipeFinderMixin {
    @WrapOperation(method = "addNormalItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z", remap = true))
    private boolean gtceu$damagedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "addNormalItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z", remap = true))
    private boolean gtceu$enchantedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }
}
