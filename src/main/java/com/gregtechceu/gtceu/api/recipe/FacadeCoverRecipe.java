package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonObject;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FacadeCoverRecipe implements CraftingRecipe {

    public static final FacadeCoverRecipe INSTANCE = new FacadeCoverRecipe();
    public static final RecipeSerializer<FacadeCoverRecipe> SERIALIZER = new RecipeSerializer<>() {

        @Override
        public FacadeCoverRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            return INSTANCE;
        }

        @Override
        public FacadeCoverRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return INSTANCE;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FacadeCoverRecipe recipe) {}
    };

    public static ResourceLocation ID = GTCEu.id("crafting/facade_cover");

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int platesCount = 0;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                if (foundBlockItem) {
                    return false;
                }
                foundBlockItem = true;
            } else if (item.is(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron))) {
                if (platesCount > 1) {
                    return false;
                }
                platesCount++;
            } else {
                return false;
            }
        }
        return foundBlockItem && platesCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryManager) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack();
        BlockState facadeState = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
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
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron)),
                Ingredient.of(Blocks.STONE));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        ItemStack result = GTItems.COVER_FACADE.asStack(6);
        FacadeItemBehaviour.setFacadeState(result, Blocks.STONE.defaultBlockState());
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
