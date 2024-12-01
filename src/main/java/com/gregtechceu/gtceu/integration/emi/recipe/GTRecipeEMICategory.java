package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.emi.IGui2Renderable;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTRecipeEMICategory extends EmiRecipeCategory {

    public static final Function<GTRecipeCategory, EmiRecipeCategory> CATEGORIES = Util.memoize(GTRecipeEMICategory::of);
    private final GTRecipeCategory category;

    private final static MethodHandle setPagesMH;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh;
        try {
            Method setPages1 = EmiApi.class.getDeclaredMethod("setPages", Map.class, EmiIngredient.class);
            setPages1.setAccessible(true);
            mh = lookup.unreflect(setPages1);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            mh = null;
        }
        setPagesMH = mh;
    }

    private GTRecipeEMICategory(GTRecipeCategory category) {
        super(category.getRecipeType().registryName, getDrawable(category), getDrawable(category));
        this.category = category;
    }

    private static EmiRecipeCategory of(GTRecipeCategory category) {
        if(category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return VanillaEmiRecipeCategories.SMELTING;
        return new GTRecipeEMICategory(category);
    }

    public static EmiRenderable getDrawable(GTRecipeCategory category) {
        if (category.getIcon() != null) {
            return IGui2Renderable.toDrawable(category.getIcon(), 16, 16);
//            return new EmiTexture(tex.imageLocation, 0, 0, 16, 16,
//                    (int) tex.imageWidth, (int) tex.imageHeight, (int) tex.imageWidth, (int) tex.imageHeight);
        } else if (category.getRecipeType().getIconSupplier() != null)
            return EmiStack.of(category.getRecipeType().getIconSupplier().get());
        else
            return EmiStack.of(Items.BARRIER);
    }

    public static void displayCategories(GTRecipeType type) {
        if (setPagesMH != null) {
            try {
                var man = EmiApi.getRecipeManager();
                var m = type.categoryStream()
                        .map(CATEGORIES)
                        .collect(Collectors.toMap(c -> c, man::getRecipes));
                setPagesMH.invokeExact(m, (EmiIngredient) EmiStack.EMPTY);
            } catch (Throwable e) {
                GTCEu.LOGGER.info("Recipe display issue {}", e.getMessage());
                EmiApi.displayRecipeCategory(CATEGORIES.apply(type.getCategory()));
            }
        } else {
            EmiApi.displayRecipeCategory(CATEGORIES.apply(type.getCategory()));
        }
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            var type = category.getRecipeType();
            if (!type.getRecipeUI().isXEIVisible() && !Platform.isDevEnv()) continue;
            EmiRecipeCategory emiCategory = CATEGORIES.apply(category);
            var recipes = type.getRecipesForCategory(category).stream();
            Stream.concat(recipes, type.getRepresentativeRecipes().stream())
                    .map(recipe -> new GTEmiRecipe(recipe, emiCategory))
                    .forEach(registry::addRecipe);
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            if (machine.getRecipeTypes() == null) continue;
            for (GTRecipeType type : machine.getRecipeTypes()) {
                if (type == null || !(Platform.isDevEnv() || type.getRecipeUI().isXEIVisible())) continue;
                for (GTRecipeCategory category : type.categorySet()) {
                    registry.addWorkstation(CATEGORIES.apply(category), EmiStack.of(machine.asStack()));
                }
            }
        }
    }

    @Override
    public Component getName() {
        return Component.translatable(category.getLanguageKey());
    }
}
