package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GTRecipeTypeCategory extends ModularUIRecipeCategory<GTRecipeWrapper> {

    public static final Function<GTRecipeType, RecipeType<GTRecipeWrapper>> TYPES = Util
            .memoize(recipeMap -> new RecipeType<>(recipeMap.registryName, GTRecipeWrapper.class));

    private final GTRecipeType recipeType;
    private final GTRecipeCategory category;
    @Getter
    private final IDrawable background;
    @Getter
    private IDrawable icon;

    private static final Map<GTRecipeCategory, GTRecipeTypeCategory> gtCategories = new Object2ObjectOpenHashMap<>();
    private static final Map<net.minecraft.world.item.crafting.RecipeType<?>, List<GTRecipeTypeCategory>> recipeTypeCategories = new Object2ObjectOpenHashMap<>();

    public GTRecipeTypeCategory(IJeiHelpers helpers, @NotNull GTRecipeType recipeType,
                                @NotNull GTRecipeCategory category) {
        this.recipeType = recipeType;
        this.category = category;
        IGuiHelper guiHelper = helpers.getGuiHelper();
        var size = recipeType.getRecipeUI().getJEISize();
        this.background = guiHelper.createBlankDrawable(size.width, size.height);
        for (GTRecipeCategory category1 : recipeType.getRecipesByCategory().keySet()) {
            Object icon = category1.getIcon();
            if (icon instanceof ResourceTexture resourceTexture) {
                icon = helpers.getGuiHelper().createDrawable(resourceTexture.imageLocation, 0, 0, 18, 18);
            } else if (recipeType.getIconSupplier() != null) {
                icon = helpers.getGuiHelper().createDrawableItemStack(recipeType.getIconSupplier().get());
            } else {
                icon = helpers.getGuiHelper().createDrawableItemStack(Items.BARRIER.getDefaultInstance());
            }
        }
        gtCategories.put(category, this);
        recipeTypeCategories.compute(recipeType, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            v.add(this);
            return v;
        });
    }

    @Override
    @NotNull
    public RecipeType<GTRecipeWrapper> getRecipeType() {
        return TYPES.apply(recipeType);
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(recipeType.registryName.toLanguageKey());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        for (net.minecraft.world.item.crafting.RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    for (Map.Entry<GTRecipeCategory, List<GTRecipe>> entry : gtRecipeType.getRecipesByCategory()
                            .entrySet()) {
                        registration.addRecipes(GTRecipeTypeCategory.TYPES.apply(entry.getKey().getRecipeType()),
                                Minecraft.getInstance().getConnection().getRecipeManager()
                                        .getAllRecipesFor(gtRecipeType)
                                        .stream()
                                        .map(GTRecipeWrapper::new)
                                        .collect(Collectors.toList()));

                        if (gtRecipeType.isScanner()) {
                            List<GTRecipe> scannerRecipes = gtRecipeType.getRepresentativeRecipes();
                            if (!scannerRecipes.isEmpty()) {
                                registration.addRecipes(GTRecipeTypeCategory.TYPES.apply(gtRecipeType),
                                        scannerRecipes.stream()
                                                .map(GTRecipeWrapper::new)
                                                .collect(Collectors.toList()));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (GTRecipeType gtRecipeType : GTRegistries.RECIPE_TYPES) {
            if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                for (MachineDefinition machine : GTRegistries.MACHINES) {
                    if (machine.getRecipeTypes() != null) {
                        for (GTRecipeType type : machine.getRecipeTypes()) {
                            for (GTRecipeCategory category : type.getRecipeByCategory().keySet()) {
                                var jeiCategory = GTRecipeTypeCategory.getCategoryFor(category);
                                if (jeiCategory != null) {
                                    if (type == gtRecipeType) {
                                        registration.addRecipeCatalyst(machine.asStack(),
                                                GTRecipeTypeCategory.TYPES.apply(jeiCategory.recipeType));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull GTRecipeWrapper wrapper) {
        return wrapper.recipe.id;
    }

    public static GTRecipeTypeCategory getCategoryFor(GTRecipeCategory category) {
        return gtCategories.get(category);
    }

    public static Collection<GTRecipeTypeCategory> getCategoriesFor(GTRecipeType recipeType) {
        return recipeTypeCategories.get(recipeType);
    }
}
