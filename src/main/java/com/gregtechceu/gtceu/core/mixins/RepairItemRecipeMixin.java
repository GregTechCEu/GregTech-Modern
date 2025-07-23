package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput container) {
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
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                     ordinal = 0),
            cancellable = true)
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

    @Inject(
            method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true)
    public void gtceu$assemble(CraftingInput input, HolderLookup.Provider registries,
                               CallbackInfoReturnable<ItemStack> cir,
                               @Local(ordinal = 0) ItemStack itemstack,
                               @Local(ordinal = 0) int maxItemDamage,
                               @Local(ordinal = 3) int calculatedDamage) {
        if (itemstack.getItem() instanceof IGTTool tool) {
            ItemStack ret = cir.getReturnValue();
            ItemEnchantments doneEnchants = EnchantmentHelper.getEnchantmentsForCrafting(ret);
            ret = tool.get();
            // re-apply the enchantments to the new item
            EnchantmentHelper.updateEnchantments(
                    ret,
                    itemEnchants -> registries.lookupOrThrow(Registries.ENCHANTMENT)
                            .listElements()
                            .filter(enchant -> enchant.is(EnchantmentTags.CURSE))
                            .forEach(enchant -> {
                                itemEnchants.upgrade(enchant, doneEnchants.getLevel(enchant));
                            }));
            ret.setDamageValue(Math.max(maxItemDamage - calculatedDamage, 0));

            cir.setReturnValue(ret);
        }
    }
}
