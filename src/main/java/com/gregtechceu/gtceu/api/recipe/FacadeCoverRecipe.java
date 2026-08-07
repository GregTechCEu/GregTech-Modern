package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;

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
import net.minecraft.world.level.block.state.BlockState;

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
        boolean foundPlate = false;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                if (foundBlockItem) {
                    return false;
                }
                foundBlockItem = true;
            } else if (item.is(IRON_PLATE_TAG)) {
                if (foundPlate) {
                    return false;
                }
                foundPlate = true;
            } else {
                return false;
            }
        }
        return foundBlockItem && foundPlate;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput container, HolderLookup.@NotNull Provider provider) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack();
        BlockState facadeState = null;

        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                facadeState = FacadeItemBehaviour.getFacadeState(item);
                break;
            }
        }
        if (facadeState != null) {
            FacadeItemBehaviour.setFacadeState(itemStack, facadeState);
            itemStack.setCount(6);
            return itemStack;
        }
        return ItemStack.EMPTY;
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
        return width * height >= 4;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        var result = GTItems.COVER_FACADE.asStack();
        FacadeItemBehaviour.setFacadeState(GTItems.COVER_FACADE.asStack(), Blocks.STONE.defaultBlockState());
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializers.CRAFTING_FACADE_COVER.get();
    }
}
