package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class GTRecipeJEICategory extends ModularUIRecipeCategory<GTRecipeWrapper> {

    public static final Function<GTRecipeCategory, RecipeType<GTRecipeWrapper>> TYPES = Util
            .memoize(category -> new RecipeType<>(category.getResourceLocation(), GTRecipeWrapper.class));

    private final GTRecipeCategory category;
    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;

    public GTRecipeJEICategory(IJeiHelpers helpers,
                               @NotNull GTRecipeCategory category) {
        this.category = category;
        var recipeType = category.getRecipeType();
        IGuiHelper guiHelper = helpers.getGuiHelper();
        var size = recipeType.getRecipeUI().getJEISize();
        this.background = guiHelper.createBlankDrawable(size.width, size.height);

        Object icon1 = category.getIcon();
        if (icon1 instanceof ResourceTexture tex) {
            this.icon = helpers.getGuiHelper()
                    .drawableBuilder(tex.imageLocation, 0, 0, (int) tex.imageWidth, (int) tex.imageHeight)
                    .setTextureSize(16, 16).build();
        } else if (recipeType.getIconSupplier() != null) {
            this.icon = helpers.getGuiHelper().createDrawableItemStack(recipeType.getIconSupplier().get());
        } else {
            this.icon = helpers.getGuiHelper().createDrawableItemStack(Items.BARRIER.getDefaultInstance());
        }
    }

    @Override
    @NotNull
    public RecipeType<GTRecipeWrapper> getRecipeType() {
        return TYPES.apply(category);
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(category.getTranslation());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            var type = category.getRecipeType();
            if (type == GTRecipeTypes.FURNACE_RECIPES) continue;
            if (!type.getRecipeUI().isXEIVisible() && !Platform.isDevEnv()) continue;
            var recipes = type.getCategoryMap().getOrDefault(category, Set.of()).stream();
            var wrapped = Stream.concat(recipes, type.getRepresentativeRecipes().stream())
                    .map(GTRecipeWrapper::new)
                    .toList();
            registration.addRecipes(TYPES.apply(category), wrapped);
        }
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            if (machine.getRecipeTypes() == null) continue;
            for (GTRecipeType type : machine.getRecipeTypes()) {
                if (type == null || !(Platform.isDevEnv() || type.getRecipeUI().isXEIVisible())) continue;
                for (GTRecipeCategory category : type.getRecipesByCategory().keySet()) {
                    registration.addRecipeCatalyst(machine.asStack(), TYPES.apply(category));
                }
            }
        }
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull GTRecipeWrapper wrapper) {
        return wrapper.recipe.id;
    }
}
