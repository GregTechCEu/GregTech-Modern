package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;

import net.minecraft.Util;
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

public class GTRecipeJEICategory extends ModularUIRecipeCategory<GTRecipeWrapper> {

    public static final Function<GTRecipeCategory, RecipeType<GTRecipeWrapper>> TYPES = Util
            .memoize(category1 -> new RecipeType<>(category1.getResourceLocation(), GTRecipeWrapper.class));

    private final GTRecipeCategory category;
    @Getter
    private final IDrawable background;
    @Getter
    private IDrawable icon;

    private static final Map<GTRecipeCategory, GTRecipeJEICategory> gtCategories = new Object2ObjectOpenHashMap<>();
    private static final Map<net.minecraft.world.item.crafting.RecipeType<?>, List<GTRecipeJEICategory>> recipeTypeCategories = new Object2ObjectOpenHashMap<>();

    public GTRecipeJEICategory(IJeiHelpers helpers, @NotNull GTRecipeType recipeType,
                               @NotNull GTRecipeCategory category) {
        this.category = category;
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
        return TYPES.apply(category);
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(category.getTranslation());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        for (net.minecraft.world.item.crafting.RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (gtRecipeType == GTRecipeTypes.FURNACE_RECIPES)
                    continue;
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    for (Map.Entry<GTRecipeCategory, List<GTRecipe>> entry : gtRecipeType.getRecipesByCategory()
                            .entrySet()) {
                        registration.addRecipes(GTRecipeJEICategory.TYPES.apply(entry.getKey()),
                                entry.getValue().stream().map(GTRecipeWrapper::new).collect(Collectors.toList()));

                        if (gtRecipeType.isScanner()) {
                            List<GTRecipe> scannerRecipes = gtRecipeType.getRepresentativeRecipes();
                            if (!scannerRecipes.isEmpty()) {
                                registration.addRecipes(GTRecipeJEICategory.TYPES.apply(entry.getKey()),
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
                                var jeiCategory = GTRecipeJEICategory.getCategoryFor(category);
                                if (jeiCategory != null) {
                                    if (type == gtRecipeType) {
                                        registration.addRecipeCatalyst(machine.asStack(),
                                                GTRecipeJEICategory.TYPES.apply(jeiCategory.category));
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

    public static GTRecipeJEICategory getCategoryFor(GTRecipeCategory category) {
        return gtCategories.get(category);
    }

    public static Collection<GTRecipeJEICategory> getCategoriesFor(GTRecipeType recipeType) {
        return recipeTypeCategories.get(recipeType);
    }
}
