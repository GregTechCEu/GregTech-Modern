package com.gregtechceu.gtceu.api.recipe.kind;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.GTRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

public class FacadeCoverRecipe extends CustomRecipe {

    public static final ResourceLocation ID = GTCEu.id("crafting/facade_cover");
    private static TagKey<Item> IRON_PLATE_TAG;

    public FacadeCoverRecipe(CraftingBookCategory category) {
        super(category);
        if (IRON_PLATE_TAG == null) {
            IRON_PLATE_TAG = ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron);
        }
    }

    @Override
    public boolean matches(CraftingInput container, @NotNull Level level) {
        int plateSize = 0;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                foundBlockItem = true;
                continue;
            }
            if (item.is(IRON_PLATE_TAG)) {
                plateSize++;
                continue;
            }
            return false;
        }
        return foundBlockItem && plateSize == 3;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput container, HolderLookup.@NotNull Provider provider) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack(3);
        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                FacadeItemBehaviour.setFacadeStack(itemStack, item);
                itemStack.setCount(6);
                break;
            }
        }
        return itemStack;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(IRON_PLATE_TAG),
                Ingredient.of(IRON_PLATE_TAG),
                Ingredient.of(IRON_PLATE_TAG),
                Ingredient.of(Items.STONE));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        var result = GTItems.COVER_FACADE.asStack();
        FacadeItemBehaviour.setFacadeStack(GTItems.COVER_FACADE.asStack(), new ItemStack(Blocks.STONE));
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializers.CRAFTING_FACADE_COVER.get();
    }
}
