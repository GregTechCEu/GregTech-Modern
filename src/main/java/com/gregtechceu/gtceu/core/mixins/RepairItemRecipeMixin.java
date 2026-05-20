package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin extends CustomRecipe {

    public RepairItemRecipeMixin(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    /**
     * It's a hack to prevent the tool from being returned
     * 
     * @param container the input inventory
     */
    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container) {
        var result = super.getRemainingItems(container);
        for (ItemStack stack : result) {
            if (stack.getItem() instanceof IGTTool) {
                stack.setCount(0);
            }
        }
        return result;
    }

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                     ordinal = 0),
            cancellable = true)
    public void gtceu$matches(CraftingContainer inv, Level worldIn, CallbackInfoReturnable<Boolean> cir,
                              @Local(ordinal = 0) ItemStack first, @Local(ordinal = 1) ItemStack second) {
        if (first.getItem() instanceof IGTTool firstTool && second.getItem() instanceof IGTTool secondTool) {
            // do not allow repairing electric tools
            if (firstTool.isElectric() || secondTool.isElectric()) {
                cir.setReturnValue(false);
            }
            // do not allow repairing tools if both have full durability
            if (!first.isDamaged() && !second.isDamaged()) {
                cir.setReturnValue(false);
            }
        }
    }

    @WrapOperation(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
                   at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack"))
    private ItemStack gtceu$copyToolItem(ItemLike item, Operation<ItemStack> original) {
        if (item instanceof IGTTool tool) {
            return tool.get();
        }
        return original.call(item);
    }
}
