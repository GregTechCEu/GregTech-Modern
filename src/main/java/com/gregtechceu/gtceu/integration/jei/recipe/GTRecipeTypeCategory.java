package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GTRecipeTypeCategory extends ModularUIRecipeCategory<GTRecipeWrapper> {
    public static final Function<GTRecipeType, RecipeType<GTRecipeWrapper>> TYPES = Util.memoize(recipeMap -> new RecipeType<>(recipeMap.registryName, GTRecipeWrapper.class));

    private final GTRecipeType recipeType;
    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;

    public GTRecipeTypeCategory(IJeiHelpers helpers, GTRecipeType recipeType) {
        this.recipeType = recipeType;
        IGuiHelper guiHelper = helpers.getGuiHelper();
        var size = recipeType.getJEISize();
        this.background = guiHelper.createBlankDrawable(size.width, size.height);
        if (recipeType.getIconSupplier() != null) {
            icon = helpers.getGuiHelper().createDrawableItemStack(recipeType.getIconSupplier().get());
        } else {
            icon = helpers.getGuiHelper().createDrawableItemStack(Items.BARRIER.getDefaultInstance());
        }
    }

    @Override
    @Nonnull
    public RecipeType<GTRecipeWrapper> getRecipeType() {
        return TYPES.apply(recipeType);
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable(recipeType.registryName.toLanguageKey());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        for (net.minecraft.world.item.crafting.RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registration.addRecipes(GTRecipeTypeCategory.TYPES.apply(gtRecipeType),
                        Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(gtRecipeType)
                        .stream()
                        .map(GTRecipeWrapper::new)
                        .collect(Collectors.toList()));
            }
        }
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (GTRecipeType gtRecipeType : GTRegistries.RECIPE_TYPES) {
            for (MachineDefinition machine : GTRegistries.MACHINES) {
                if (machine.getRecipeTypes() != null) {
                    for (GTRecipeType type : machine.getRecipeTypes()){
                        if (type == gtRecipeType) {
                            registration.addRecipeCatalyst(machine.asStack(), GTRecipeTypeCategory.TYPES.apply(gtRecipeType));
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
}
