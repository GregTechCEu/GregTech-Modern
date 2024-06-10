package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.material.GTMaterials;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.mojang.serialization.MapCodec;

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
    public static final MapCodec<FacadeCoverRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeCoverRecipe> STREAM_CODEC = StreamCodec
            .unit(INSTANCE);
    public static final RecipeSerializer<FacadeCoverRecipe> SERIALIZER = new RecipeSerializer<>() {

        @Override
        public MapCodec<FacadeCoverRecipe> codec() {
            return FacadeCoverRecipe.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FacadeCoverRecipe> streamCodec() {
            return FacadeCoverRecipe.STREAM_CODEC;
        }
    };

    public static ResourceLocation ID = GTCEu.id("crafting/facade_cover");

    @Override
    public boolean matches(CraftingInput container, Level level) {
        int plateSize = 0;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.size(); i++) {
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
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider provider) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack(3);
        for (int i = 0; i < container.size(); i++) {
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
        TagKey<Item> ironPlate = ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron);
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ironPlate),
                Ingredient.of(ironPlate),
                Ingredient.of(ironPlate),
                Ingredient.of(Blocks.STONE));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        var result = GTItems.COVER_FACADE.asStack();
        FacadeItemBehaviour.setFacadeStack(GTItems.COVER_FACADE.asStack(), new ItemStack(Blocks.STONE));
        return result;
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
