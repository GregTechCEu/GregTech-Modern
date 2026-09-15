package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.data.recipe.builder.EquipmentFoundryRecipeBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class EquipmentFoundryRecipeHelper {

    private static Ingredient objectToIngredient(Object ingredient) {
        if (ingredient instanceof Ingredient ing) {
            return ing;
        } else if (ingredient instanceof ItemStack itemStack) {
            return Ingredient.of(itemStack);
        } else if (ingredient instanceof TagKey<?> key) {
            return Ingredient.of(key.cast(Registries.ITEM).orElseThrow(
                    () -> new ClassCastException("Cannot add %s tag as ingredient".formatted(key.registry()))));
        } else if (ingredient instanceof ItemLike itemLike) {
            return Ingredient.of(itemLike);
        }
        return Ingredient.EMPTY;
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                                 @NotNull Ingredient equipment,
                                                 Consumer<EquipmentFoundryRecipeBuilder> builderConsumer) {
        var builder = new EquipmentFoundryRecipeBuilder(regName).equipment(equipment);
        builderConsumer.accept(builder);
        builder.save(provider);
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                                 @NotNull Ingredient equipment,
                                                 @Nullable Object[] ingredients, @Nullable ItemModuleType<?>[] modules) {
        var ingArr = Arrays.copyOf(ingredients, GTValues.TIER_COUNT);
        ItemModuleType<?>[] moduleArr = Arrays.copyOf(modules, GTValues.TIER_COUNT);
        var builder = new EquipmentFoundryRecipeBuilder(regName).equipment(equipment);
        for (int i=0; i<GTValues.TIER_COUNT; i++) {
            builder.ingredient(i, objectToIngredient(ingArr[i]));
            builder.module(i, moduleArr[i]);
        }
        builder.save(provider);
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                                 @NotNull Ingredient equipment,
                                                 @NotNull Object ingredient, @NotNull ItemModuleType<?> modifier) {
        var builder = new EquipmentFoundryRecipeBuilder(regName);
        builder.equipment(equipment);
        builder.tier(0, objectToIngredient(ingredient), modifier);
        builder.save(provider);
    }
}
