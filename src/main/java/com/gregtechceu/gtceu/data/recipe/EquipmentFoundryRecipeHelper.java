package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
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

import java.util.function.Consumer;

public class EquipmentFoundryRecipeHelper {

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                                 @NotNull Ingredient equipment,
                                                 @NotNull Object ingredient, @NotNull ItemModuleType<?>[] modifiers) {
        var builder = new EquipmentFoundryRecipeBuilder(regName).equipment(equipment).modifier(modifiers);
        if (ingredient instanceof Ingredient ing) {
            builder.ingredient(ing);
        } else if (ingredient instanceof ItemStack itemStack) {
            builder.ingredient(itemStack);
        } else if (ingredient instanceof TagKey<?> key) {
            builder.ingredient(key.cast(Registries.ITEM).orElseThrow(
                    () -> new ClassCastException("Cannot add %s tag as ingredient".formatted(key.registry()))));
        } else if (ingredient instanceof ItemLike itemLike) {
            builder.ingredient(itemLike);
        } else if (ingredient instanceof MaterialEntry entry) {
            TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix(), entry.material());
            if (tag != null) {
                builder.ingredient(tag);
            } else builder.ingredient(ChemicalHelper.get(entry.tagPrefix(), entry.material()));
        } else if (ingredient instanceof Character c) {
            builder.ingredient(ToolHelper.getToolFromSymbol(c).itemTags.get(0));
        }
        builder.save(provider);
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                                 @NotNull Ingredient equipment,
                                                 @NotNull Object ingredient, @NotNull ItemModuleType<?>[] modifiers) {
        addEquipmentFoundryRecipe(provider, GTCEu.id(regName), equipment, ingredient, modifiers);
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                                 @NotNull Ingredient equipment,
                                                 @NotNull Object ingredient, @NotNull ItemModuleType<?> modifier) {
        addEquipmentFoundryRecipe(provider, GTCEu.id(regName), equipment, ingredient, modifier);
    }

    public static void addEquipmentFoundryRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                                 @NotNull Ingredient equipment,
                                                 @NotNull Object ingredient, @NotNull ItemModuleType<?> modifier) {
        addEquipmentFoundryRecipe(provider, regName, equipment, ingredient, new ItemModuleType<?>[] { modifier });
    }
}
