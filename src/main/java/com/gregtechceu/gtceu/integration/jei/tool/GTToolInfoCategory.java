package com.gregtechceu.gtceu.integration.jei.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTToolInfoCategory extends ModularUIRecipeCategory<GTToolInfoWrapper> {

    public final static RecipeType<GTToolInfoWrapper> RECIPE_TYPE = new RecipeType<>(GTCEu.id("tool_diagram"), GTToolInfoWrapper.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GTToolInfoCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(186, 174);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(ToolHelper.get(GTToolType.CROWBAR, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTCEuAPI.materialManager.getRegisteredMaterials().stream()
                .filter((m) -> m.hasProperty(PropertyKey.TOOL))
                .map(GTToolInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

    }

    @NotNull
    @Override
    public RecipeType<GTToolInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.tool_page");
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
