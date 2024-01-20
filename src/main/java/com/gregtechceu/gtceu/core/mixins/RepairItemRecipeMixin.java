package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin extends CustomRecipe {

    public RepairItemRecipeMixin(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    /**
     * It's a hack to prevent the tool from being returned
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
        method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0),
        cancellable = true
    )
    public void gtceu$matches(CraftingContainer inv, Level worldIn, CallbackInfoReturnable<Boolean> cir,
                              @Local(ordinal = 0) ItemStack itemstack,
                              @Local(ordinal = 1) ItemStack itemstack1) {
        if (itemstack.getItem() instanceof IGTTool first && itemstack1.getItem() instanceof IGTTool second) {
            // do not allow repairing tools if are electric
            if (first.isElectric() || second.isElectric()) {
                cir.setReturnValue(false);
            }
            // do not allow repairing tools if both are full durability
            if (!first.definition$isDamaged(itemstack) && !second.definition$isDamaged(itemstack1)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
        method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "RETURN", ordinal = 1), cancellable = true
    )
    public void gtceu$assemble(CraftingContainer container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir,
                               @Local(ordinal = 1) ItemStack itemstack3,
                               @Local(ordinal = 2) LocalRef<ItemStack> itemstack2,
                               @Local(ordinal = 3) int i1,
                               @Local(ordinal = 0) Map<Enchantment, Integer> map) {
        if (itemstack3.getItem() instanceof IGTTool tool) {
            itemstack2.set(tool.get());
            itemstack2.get().setDamageValue(i1);

            // apply curse enchantments properly
            if (!map.isEmpty()) {
                map.forEach((enchantment, level) -> itemstack2.get().enchant(enchantment, level));
            }

            cir.setReturnValue(itemstack2.get());
        }
    }
}
