package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin extends CustomRecipe {

    public RepairItemRecipeMixin(CraftingBookCategory category) {
        super(category);
    }

    /**
     * It's a hack to prevent the tool from being returned
     * 
     * @param container the input inventory
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        var result = super.getRemainingItems(container);
        for (ItemStack stack : result) {
            if (stack.getItem() instanceof IGTTool) {
                stack.setCount(0);
            }
        }
        return result;
    }

    @Inject(
        method = "canCombine(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0),
        cancellable = true
    )
    private static void gtceu$matches(ItemStack itemStack, ItemStack itemStack1, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.getItem() instanceof IGTTool first && itemStack1.getItem() instanceof IGTTool second) {
            // do not allow repairing tools if are electric
            if (first.isElectric() || second.isElectric()) {
                cir.setReturnValue(false);
            }
            // do not allow repairing tools if both are full durability
            if (!itemStack.isDamaged() && !itemStack1.isDamaged()) {
                cir.setReturnValue(false);
            }
        }
    }

    /*
    @Inject(
        method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "RETURN", ordinal = 1), cancellable = true
    )
    public void gtceu$assemble(CraftingContainer container, HolderLookup.Provider registryAccess, CallbackInfoReturnable<ItemStack> cir,
                               @Local(ordinal = 1) ItemStack itemstack3,
                               @Local(ordinal = 2) LocalRef<ItemStack> itemstack2,
                               @Local(ordinal = 3) int i1,
                               @Local(ordinal = 0) ItemEnchantments first,
                               @Local(ordinal = 1) ItemEnchantments second) {
        if (itemstack3.getItem() instanceof IGTTool tool) {
            itemstack2.set(tool.get());
            itemstack2.get().setDamageValue(i1);

            // apply curse enchantments properly
            if (!first.isEmpty()) {
                first.forEach((enchantment, level) -> itemstack2.get().enchant(enchantment, level));
            }

            cir.setReturnValue(itemstack2.get());
        }
    }
    */
}
