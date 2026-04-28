package com.lowdragmc.lowdraglib.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static IJeiRuntime jeiRuntime;
    public static IJeiHelpers jeiHelpers;

    public static Object getItemIngredient(ItemStack stack, int x, int y, int width, int height) {
        if (stack.isEmpty() || jeiHelpers == null) {
            return null;
        }
        return jeiHelpers.getIngredientManager()
                .createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false)
                .map(typed -> new ClickableIngredient<>(typed, x, y, width, height))
                .orElse(null);
    }

    public static boolean isJeiEnabled() {
        return jeiRuntime != null || jeiHelpers != null;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("ldlib", "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }
}
