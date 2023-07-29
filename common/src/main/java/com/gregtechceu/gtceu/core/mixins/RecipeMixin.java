package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.GTToolItem;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author KilaBash
 * @date 2023/7/29
 * @implNote RecipeMixin
 */
@Mixin(RepairItemRecipe.class)
public abstract class RecipeMixin extends CustomRecipe {

    public RecipeMixin(ResourceLocation id, CraftingBookCategory category) {
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
            if (stack.getItem() instanceof GTToolItem) {
                stack.setCount(0);
            }
        }
        return result;
    }
}
