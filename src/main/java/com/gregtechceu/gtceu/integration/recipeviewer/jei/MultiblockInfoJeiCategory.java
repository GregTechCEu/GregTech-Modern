package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockInfoJeiCategory extends ModularUIJeiCategory<MultiblockMachineDefinition> {

    public final static RecipeType<MultiblockMachineDefinition> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("multiblock_info"),
            MultiblockMachineDefinition.class);

    private final IDrawable icon;

    public MultiblockInfoJeiCategory(IJeiHelpers helpers) {
        super(v -> new MultiblockPreviewWidget(v, null, 200, 180), MachineDefinition::getId);
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

    @Override
    public @Nullable ResourceLocation getRegistryName(MultiblockMachineDefinition recipe) {
        return recipe.getId();
    }

    @Override
    public RecipeType<MultiblockMachineDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public int getMaxWidth() {
        return 200;
    }

    @Override
    public int getMaxHeight() {
        return 180;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, MultiblockMachineDefinition definition,
                                       IFocusGroup focuses) {
        List<ItemStack> containedBlocks = MultiblockPreviewWidget.initializeContainedBlocks(definition);

        builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(definition.getBlock()));

        for (var stack : containedBlocks) {
            builder.addSlot(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK, stack);
        }
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
