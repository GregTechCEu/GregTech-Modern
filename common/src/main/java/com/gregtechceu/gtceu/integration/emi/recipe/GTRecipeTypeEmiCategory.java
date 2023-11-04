package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.emi.ModularUIEmiRecipeCategory;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Function;

public class GTRecipeTypeEmiCategory extends ModularUIEmiRecipeCategory {
    public static final Function<GTRecipeType, GTRecipeTypeEmiCategory> CATEGORIES = Util.memoize(GTRecipeTypeEmiCategory::new);
    public final GTRecipeType recipeType;

    public GTRecipeTypeEmiCategory(GTRecipeType recipeType) {
        super(recipeType.registryName, recipeType.getIconSupplier() != null ? EmiStack.of(recipeType.getIconSupplier().get()) : EmiStack.of(Items.BARRIER));
        this.recipeType = recipeType;
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(gtRecipeType).stream()
                        .map(recipe -> new GTEmiRecipe(CATEGORIES.apply(gtRecipeType), recipe))
                        .forEach(registry::addRecipe);
            }
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (GTRecipeType gtRecipeType : GTRegistries.RECIPE_TYPES) {
            for (MachineDefinition machine : GTRegistries.MACHINES) {
                if (machine.getRecipeTypes() != null) {
                    for (GTRecipeType type : machine.getRecipeTypes()){
                        if (type == gtRecipeType) {
                            registry.addWorkstation(GTRecipeTypeEmiCategory.CATEGORIES.apply(gtRecipeType), EmiStack.of(machine.asStack()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Component getName() {
        return Component.translatable(recipeType.registryName.toLanguageKey());
    }
}
