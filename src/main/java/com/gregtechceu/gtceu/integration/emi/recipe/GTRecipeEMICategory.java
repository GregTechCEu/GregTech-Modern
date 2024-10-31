package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeJEICategory;
import com.lowdragmc.lowdraglib.Platform;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.runtime.EmiDrawContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GTRecipeEMICategory extends EmiRecipeCategory {

    public static final Function<GTRecipeCategory, List<GTEmiRecipe>> CATEGORIES = Util
            .memoize(category1 -> new ArrayList<>());
    public final GTRecipeType recipeType;

    private final GTRecipeCategory category;
    private Object icon;
    private static final Map<GTRecipeCategory, GTRecipeEMICategory> gtCategories = new Object2ObjectOpenHashMap<>();
    private static final Map<net.minecraft.world.item.crafting.RecipeType<?>, List<GTRecipeEMICategory>> recipeTypeCategories = new Object2ObjectOpenHashMap<>();
    private static final Map<GTRecipeCategory, EmiTexture> textureMap = new Object2ObjectOpenHashMap<>();

    public GTRecipeEMICategory(GTRecipeType recipeType, @NotNull GTRecipeCategory category) {
        super(recipeType.registryName, getDrawable(category), getSimplified(category, 16, 16));
        this.recipeType = recipeType;
        this.category = category;
        gtCategories.put(category, this);
        recipeTypeCategories.compute(recipeType, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            v.add(this);
            return v;
        });
    }

    public static EmiRenderable getDrawable(GTRecipeCategory category) {
        if (category.getIcon() instanceof ResourceTexture tex) {
            var t = new EmiTexture(tex.imageLocation, 0, 0, 16, 16);
            textureMap.put(category, t);
            EmiRenderable renderable = (g, x, y, d) -> {
                t.render(g, x + 1, y + 1, d);
            };
            return renderable;
        }
        else if (category.getRecipeType().getIconSupplier() != null)
            return EmiStack.of(category.getRecipeType().getIconSupplier().get());
        else
            return EmiStack.of(Items.BARRIER);
    }

    public static EmiRenderable getSimplified(GTRecipeCategory category, int u, int v) {
        if (category.getIcon() instanceof ResourceTexture tex) {
            var t = new EmiTexture(tex.imageLocation, 0, 0, 16, 16);
            textureMap.put(category, t);
            EmiRenderable r = (g, x, y, d) -> {
                EmiDrawContext context = EmiDrawContext.wrap(g);
                context.drawTexture(category.getResourceLocation(), x, y, u, v, 16, 16);
            };
            return r;
        }
        else if (category.getRecipeType().getIconSupplier() != null)
            return getDrawable(category);
        else
            return EmiStack.of(Items.BARRIER);
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    for (Map.Entry<GTRecipeCategory, List<GTRecipe>> entry : gtRecipeType.getRecipesByCategory().entrySet()) {
                        entry.getValue().stream()
                                .map(recipe -> new GTEmiRecipe(gtCategories.get(entry.getKey()), recipe))
                                .forEach(registry::addRecipe);

                        if (gtRecipeType.isScanner()) {
                            List<GTRecipe> scannerRecipes = gtRecipeType.getRepresentativeRecipes();
                            if (!scannerRecipes.isEmpty()) {
                                scannerRecipes.stream()
                                        .map(recipe -> new GTEmiRecipe(gtCategories.get(entry.getKey()), recipe))
                                        .forEach(registry::addRecipe);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (GTRecipeType gtRecipeType : GTRegistries.RECIPE_TYPES) {
            if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                for (MachineDefinition machine : GTRegistries.MACHINES) {
                    if (machine.getRecipeTypes() != null) {
                        for (GTRecipeType type : machine.getRecipeTypes()) {
                            for (GTRecipeCategory category : type.getRecipeByCategory().keySet()) {
                                var emiCategory = GTRecipeEMICategory.getCategoryFor(category);
                                if (emiCategory != null) {
                                    if (type == gtRecipeType) {
                                        registry.addWorkstation(emiCategory, EmiStack.of(machine.asStack()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static GTRecipeEMICategory getCategoryFor(GTRecipeCategory category) {
        return gtCategories.get(category);
    }

    @Override
    public Component getName() {
        return Component.translatable(category.getTranslation());
    }
}
