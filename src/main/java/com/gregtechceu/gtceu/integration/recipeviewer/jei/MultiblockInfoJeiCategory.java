package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockInfoJeiCategory extends
                                       ModularUIRecipeCategory<MultiblockMachineDefinition> {

    public final static RecipeType<MultiblockMachineDefinition> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("multiblock_info"),
            MultiblockMachineDefinition.class);
    private final IDrawable icon;

    public MultiblockInfoJeiCategory(IJeiHelpers helpers) {
        super(v -> new MultiblockPreviewWidget(v, null, 200, 180), v -> v.getId());
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .toList());
    }

    public int getMaxWidth() {
        return 200;
    }

    public int getMaxHeight() {
        return 180;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockMachineDefinition definition,
                          IFocusGroup focuses) {
        List<ItemStack> containedBlocks = MultiblockPreviewWidget.initializeContainedBlocks(definition);

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(definition.getBlock()));

        for (var stack : containedBlocks) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK, stack);
        }
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(MultiblockMachineDefinition recipe) {
        return recipe.getId();
    }

    @Override
    @NotNull
    public RecipeType<MultiblockMachineDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
