package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote FacadeCoverRecipe
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FacadeCoverRecipe implements CraftingRecipe {

    public static final FacadeCoverRecipe INSTANCE = new FacadeCoverRecipe();
    public static final SimpleRecipeSerializer<FacadeCoverRecipe> SERIALIZER = new SimpleRecipeSerializer<>(id -> INSTANCE);


    public static ResourceLocation ID = GTCEu.id("crafting/facade_cover");

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int plateSize = 0;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                foundBlockItem = true;
                continue;
            }
            if (item.is(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron))) {
                plateSize++;
                continue;
            }
            return false;
        }
        return foundBlockItem && plateSize == 3;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack();
        for (int i = 0; i < container.getContainerSize(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                FacadeItemBehaviour.setFacadeStack(itemStack, item);
                break;
            }
        }
        return itemStack;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron)),
                Ingredient.of(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron)),
                Ingredient.of(ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron)),
                Ingredient.of(Blocks.STONE));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        var result = GTItems.COVER_FACADE.asStack();
        FacadeItemBehaviour.setFacadeStack(GTItems.COVER_FACADE.asStack(), new ItemStack(Blocks.STONE));
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

}
