package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GTRecipeREICategory extends ModularUIDisplayCategory<GTRecipeDisplay> {

    public static final Function<GTRecipeCategory, CategoryIdentifier<GTRecipeDisplay>> CATEGORIES = Util
            .memoize(category -> CategoryIdentifier.of(category.getResourceLocation()));

    private final GTRecipeCategory category;
    @Getter
    private final Renderer icon;
    @Getter
    private final Size size;

    public GTRecipeREICategory(@NotNull GTRecipeCategory category) {
        this.category = category;
        var recipeType = category.getRecipeType();
        var size = recipeType.getRecipeUI().getJEISize();
        this.size = new Size(size.width + 8, size.height + 8);
        if (category.getIcon() instanceof ResourceTexture tex) {
            icon = IGui2Renderer.toDrawable(tex);
        } else if (recipeType.getIconSupplier() != null) {
            icon = IGui2Renderer.toDrawable(new ItemStackTexture(recipeType.getIconSupplier().get()));
        } else {
            icon = IGui2Renderer.toDrawable(new ItemStackTexture(Items.BARRIER.getDefaultInstance()));
        }
    }

    @Override
    public CategoryIdentifier<? extends GTRecipeDisplay> getCategoryIdentifier() {
        return CATEGORIES.apply(category);
    }

    @Override
    public int getDisplayHeight() {
        return getSize().height;
    }

    @Override
    public int getDisplayWidth(GTRecipeDisplay display) {
        return getSize().width;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(category.getTranslation());
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            var type = category.getRecipeType();
            if (type == GTRecipeTypes.FURNACE_RECIPES) continue;
            if (!type.getRecipeUI().isXEIVisible() && !Platform.isDevEnv()) continue;
            registry.registerRecipeFiller(GTRecipe.class, type, GTRecipeDisplay::new);
            type.getRepresentativeRecipes().stream()
                    .map(r -> new GTRecipeDisplay(r, category))
                    .forEach(registry::add);
        }
    }

    public static void registerWorkStations(CategoryRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            if (machine.getRecipeTypes() == null) continue;
            for (GTRecipeType type : machine.getRecipeTypes()) {
                if (type == null || !(Platform.isDevEnv() || type.getRecipeUI().isXEIVisible())) continue;
                for (GTRecipeCategory category : type.getRecipesByCategory().keySet()) {
                    registry.addWorkstations(CATEGORIES.apply(category), EntryStacks.of(machine.asStack()));
                }
            }
        }
    }
}
