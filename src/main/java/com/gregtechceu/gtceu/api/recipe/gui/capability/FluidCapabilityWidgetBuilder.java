package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

public class FluidCapabilityWidgetBuilder implements CapabilityWidgetBuilder<RecipeViewerSlotWidget<?>> {

    public static final FluidCapabilityWidgetBuilder INSTANCE = new FluidCapabilityWidgetBuilder();

    @Override
    public RecipeViewerSlotWidget<?> buildDefaultWidget() {
        return RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY));
    }

    @Override
    public void buildWidgetContent(IWidget widget, int contentIndex, Content content, IO io, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier) {
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
    }
}
