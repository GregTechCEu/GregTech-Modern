package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @WrapOperation(method = "createResult",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$checkToolEnchantValidity(Enchantment enchant, ItemStack stack,
                                                   Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool tool) {
            return tool.definition$canApplyAtEnchantingTable(stack, enchant);
        }
        return original.call(enchant, stack);
    }
}
