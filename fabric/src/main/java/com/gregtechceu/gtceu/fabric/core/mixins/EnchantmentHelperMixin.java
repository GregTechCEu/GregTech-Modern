package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
    private static int gtceu$stackSensitiveEnchantValue(int original, RandomSource random, int enchantNum, int power, ItemStack stack) {
        if (stack.getItem() instanceof IGTFabricItem gtItem) {
            return gtItem.getEnchantmentValue(stack);
        }
        return original;
    }

    @ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
    private static int gtceu$stackSensitiveSelectEnchant(int original, RandomSource random, ItemStack itemStack, int level, boolean allowTreasure) {
        if (itemStack.getItem() instanceof IGTFabricItem gtItem) {
            return gtItem.getEnchantmentValue(itemStack);
        }
        return original;
    }
}
