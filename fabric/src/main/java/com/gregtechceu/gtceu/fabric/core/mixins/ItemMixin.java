package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(Item.class)
public class ItemMixin {

    @ModifyExpressionValue(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxStackSize()I"))
    private int gtceu$stackSensitiveEnchantability(int original, ItemStack stack) {
        if (this instanceof IGTFabricItem gtItem) {
            return gtItem.getMaxStackSize(stack);
        }
        return original;
    }
}
