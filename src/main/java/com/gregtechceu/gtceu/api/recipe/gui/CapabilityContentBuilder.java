package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

/**
 * Fills recipe viewer UI slots with the capability content for a specific recipe.
 */
@FunctionalInterface
public interface CapabilityContentBuilder {

    /**
     * Fills a recipe viewer slot with capability content.
     * @param widget The widget to attempt to attach content to.
     * @param contentIndex Index of the content in the recipe input/output list.
     * @param content The content value.
     * @param io If this content is a recipe input or output.
     * @param recipeType The type of the recipe this content is for.
     * @param recipe The recipe this content is for.
     * @param recipeTier The tier this recipe should be previewed at
     * @param chanceTier The chance tier this recipe should be previewed at
     */
    void buildWidgetContent(IWidget widget, int contentIndex, Content content, IO io, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier);

    CapabilityContentBuilder ITEM = (widget, contentIndex, content, io, recipeType, recipe, recipeTier, chanceTier) -> {
        if (!(widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget)) return;

        float chance = (float) recipeType.getChanceFunction()
                .getBoostedChance(content, recipeTier, chanceTier) / content.maxChance();
        var innerContent = ItemRecipeCapability.CAP.of(content.content());
        boolean perTick = ItemRecipeCapability.CAP.isTickSlot(contentIndex, io, recipe);


        recipeViewerSlotWidget.value(ItemRecipeCapability.mapIngredientToEntryList(innerContent));
        recipeViewerSlotWidget.overlay(new ContentOverlay(content, perTick, recipeTier, chanceTier, recipeType.getChanceFunction()));
        recipeViewerSlotWidget.chance(chance);

        if (io == IO.IN && (content.chance() == 0 || innerContent instanceof IntCircuitIngredient)) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.CATALYST);
        } else if (io == IO.IN) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.INPUT);
        } else {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.OUTPUT);
        }

        recipeViewerSlotWidget.tooltipBuilder((tooltip) -> {

            Content.addChanceTooltips(tooltip, content,
                    recipe.getChanceLogicForCapability(ItemRecipeCapability.CAP, io, perTick),
                    recipeTier, chanceTier, recipeType.getChanceFunction());

            if (innerContent instanceof IntProviderIngredient ingredient) {
                IntProvider countProvider = ingredient.getCountProvider();
                tooltip.add(Component.translatable("gtceu.gui.content.count_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                        .withStyle(ChatFormatting.GOLD));
            } else if (innerContent instanceof SizedIngredient sizedIngredient &&
                    sizedIngredient.getInner() instanceof IntProviderIngredient ingredient) {

                IntProvider countProvider = ingredient.getCountProvider();
                tooltip.add(Component.translatable("gtceu.gui.content.count_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                        .withStyle(ChatFormatting.GOLD));
            }
            if (perTick) {
                tooltip.add(Component.translatable("gtceu.gui.content.per_tick"));
            }
        });
    };

    CapabilityContentBuilder FLUID = (widget, contentIndex, content, io, recipeType, recipe, recipeTier, chanceTier) -> {
        if (!(widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget)) return;

        float chance = (float) recipeType.getChanceFunction()
                .getBoostedChance(content, recipeTier, chanceTier) / content.maxChance();
        FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content());
        boolean perTick = FluidRecipeCapability.CAP.isTickSlot(contentIndex, io, recipe);

        recipeViewerSlotWidget.value(FluidRecipeCapability.mapIngredientToEntryList(ingredient));
        recipeViewerSlotWidget.overlay(new ContentOverlay(content, perTick, recipeTier, chanceTier, recipeType.getChanceFunction()));
        recipeViewerSlotWidget.chance(chance);


        recipeViewerSlotWidget.tooltipBuilder((tooltip) -> {
            if (ingredient.getStacks().length > 0) {
                FluidStack stack = ingredient.getStacks()[0];
                TooltipsHandler.appendFluidTooltips(stack, tooltip::addLine, TooltipFlag.NORMAL);
            }
            if (ingredient instanceof IntProviderFluidIngredient provider) {
                IntProvider countProvider = provider.getCountProvider();
                tooltip.addLine(Component.translatable("gtceu.gui.content.fluid_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                        .withStyle(ChatFormatting.GOLD));
            }
            if (perTick) {
                tooltip.addLine(Component.translatable("gtceu.gui.content.per_tick"));
            }
        });

        if (io == IO.IN && (content.chance() == 0)) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.CATALYST);
        } else if (io == IO.IN) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.INPUT);
        } else {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.OUTPUT);
        }
    };
}
