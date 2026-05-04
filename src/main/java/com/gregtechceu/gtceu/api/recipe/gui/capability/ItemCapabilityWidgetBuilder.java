package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;

public class ItemCapabilityWidgetBuilder implements CapabilityWidgetBuilder<RecipeViewerSlotWidget<?>> {

    public static final ItemCapabilityWidgetBuilder INSTANCE = new ItemCapabilityWidgetBuilder();

    @Override
    public RecipeViewerSlotWidget<?> buildDefaultWidget() {
        return RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY));
    }

    @Override
    public void buildWidgetContent(IWidget widget, int contentIndex, Content content, IO io, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier) {
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
    }
}
