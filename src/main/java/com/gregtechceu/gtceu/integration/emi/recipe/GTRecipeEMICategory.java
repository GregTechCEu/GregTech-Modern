package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class GTRecipeEMICategory extends EmiRecipeCategory {

    public static final Function<GTRecipeCategory, GTRecipeEMICategory> CATEGORIES = Util
            .memoize(GTRecipeEMICategory::new);
    private final GTRecipeCategory category;

    public GTRecipeEMICategory(GTRecipeCategory category) {
        super(category.getRecipeType().registryName, getDrawable(category), getDrawable(category));
        this.category = category;
    }

    public static EmiRenderable getDrawable(GTRecipeCategory category) {
        if (category.getIcon() instanceof ResourceTexture tex) {
            return new EmiTexture(tex.imageLocation, 0, 0, 16, 16,
                    (int) tex.imageWidth, (int) tex.imageHeight, (int) tex.imageWidth, (int) tex.imageHeight);
        } else if (category.getRecipeType().getIconSupplier() != null)
            return EmiStack.of(category.getRecipeType().getIconSupplier().get());
        else
            return EmiStack.of(Items.BARRIER);
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            var type = category.getRecipeType();
            if (type == GTRecipeTypes.FURNACE_RECIPES) continue;
            if (!type.getRecipeUI().isXEIVisible() && !Platform.isDevEnv()) continue;
            var recipes = type.getCategoryMap().getOrDefault(category, Set.of()).stream();
            Stream.concat(recipes, type.getRepresentativeRecipes().stream())
                    .map(recipe -> new GTEmiRecipe(CATEGORIES.apply(category), recipe))
                    .forEach(registry::addRecipe);
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            if (machine.getRecipeTypes() == null) continue;
            for (GTRecipeType type : machine.getRecipeTypes()) {
                if (type == null || !(Platform.isDevEnv() || type.getRecipeUI().isXEIVisible())) continue;
                for (GTRecipeCategory category : type.getRecipesByCategory().keySet()) {
                    registry.addWorkstation(CATEGORIES.apply(category), EmiStack.of(machine.asStack()));
                }
            }
        }
    }

    @Override
    public Component getName() {
        return Component.translatable(category.getTranslation());
    }
}
