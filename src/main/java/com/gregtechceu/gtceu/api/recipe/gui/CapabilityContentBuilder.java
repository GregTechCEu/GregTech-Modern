package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

/**
 * Fills recipe viewer UI slots with the capability content for a specific recipe.
 */
@FunctionalInterface
public interface CapabilityContentBuilder {

    /**
     * Fills a recipe viewer slot with capability content.
     *
     * @param widget     The widget to attempt to attach content to.
     * @param content    The content value.
     * @param io         If this content is a recipe input or output.
     * @param perTick    If this content is a tick input.
     * @param recipeType The type of the recipe this content is for.
     * @param recipe     The recipe this content is for.
     * @param chanceTier The chance tier this recipe should be previewed at
     * @param recipeTier The tier this recipe should be previewed at
     */
    void buildWidgetContent(IWidget widget, Content content, IO io, boolean perTick,
                            GTRecipeType recipeType, GTRecipe recipe, int chanceTier, int recipeTier);

    CapabilityContentBuilder ITEM = (widget, content, io, perTick,
                                     recipeType, recipe, chanceTier, recipeTier) -> {
        if (!(widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget)) return;

        float chance = (float) content.chance() / content.maxChance();
        var innerContent = ItemRecipeCapability.CAP.of(content.content());

        recipeViewerSlotWidget.value(ItemRecipeCapability.mapIngredientToEntryList(innerContent));
        recipeViewerSlotWidget
                .overlay(new ContentOverlay(content, perTick));
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
                    recipe.getChanceLogicForCapability(ItemRecipeCapability.CAP, io, perTick));

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

    CapabilityContentBuilder FLUID = (widget, content, io, perTick,
                                      recipeType, recipe, chanceTier, recipeTier) -> {
        if (!(widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget)) return;

        float chance = (float) content.chance() / content.maxChance();
        FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content());

        recipeViewerSlotWidget.value(FluidRecipeCapability.mapIngredientToEntryList(ingredient));
        recipeViewerSlotWidget
                .overlay(new ContentOverlay(content, perTick));
        recipeViewerSlotWidget.chance(chance);

        recipeViewerSlotWidget.tooltipBuilder((tooltip) -> {
            if (ingredient.getStacks().length > 0) {
                FluidStack stack = ingredient.getStacks()[0];
                TooltipsHandler.appendFluidTooltips(stack, tooltip::addLine, TooltipFlag.NORMAL);
            }
            if (ingredient instanceof IRangedIngredient provider) {
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

    CapabilityContentBuilder COMPUTATION = (widget, content, io, perTick,
                                            recipeType, recipe, chanceTier, recipeTier) -> {
        if (!(widget instanceof Flow flow)) return;

        var existingCompTickWidget = WidgetTree.findFirstWithNameNullable(flow, "comp_tick");
        var existingCompTotal = WidgetTree.findFirstWithNameNullable(flow, "comp_total");

        if (recipe.tickInputs.get(CWURecipeCapability.CAP) != null) {
            if (CWURecipeCapability.CAP.isTickSlot(0, IO.IN, recipe)) {
                int cwu = recipe.getTickInputContents(CWURecipeCapability.CAP).stream().map(Content::content)
                        .mapToInt(CWURecipeCapability.CAP::of).sum();
                var text = Text.lang("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu));

                if (existingCompTickWidget != null) ((TextWidget<?>) existingCompTickWidget).value(text);
                else flow.child(text.asWidget().name("comp_tick"));
            }
            if (recipe.data.getBoolean("duration_is_total_cwu")) {
                var text = Text.lang("gtceu.recipe.total_computation", FormattingUtil.formatNumbers(recipe.duration));

                if (existingCompTotal != null) ((TextWidget<?>) existingCompTotal).value(text);
                else flow.child(text.asWidget().name("comp_total"));
            }
        }
    };

    CapabilityContentBuilder EU = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
        if (!(widget instanceof Flow flow)) return;

        var eu = RecipeHelper.getRealEUt(recipe);

        var minVoltageTier = GTUtil.getTierByVoltage(eu.voltage());
        float minAmperage = (float) eu.getTotalEU() / GTValues.V[minVoltageTier];

        if (eu.voltage() > 0) {

            var maxEuWidget = WidgetTree.findFirstWithNameNullable(flow, "max_eu");
            var euWidget = WidgetTree.findFirstWithNameNullable(flow, "eu");

            ModularComponent maxEu;

            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.data.getBoolean("duration_is_total_cwu") &&
                    recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.max(recipe.tickInputs.get(CWURecipeCapability.CAP).stream()
                        .map(Content::content).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                maxEu = Text.lang("gtceu.recipe.max_eu",
                        FormattingUtil.formatNumbers(eu.getTotalEU() / minimumCWUt));
            } else {
                maxEu = Text.lang("gtceu.recipe.total",
                        FormattingUtil.formatNumbers(eu.getTotalEU() * recipe.duration));
            }

            if (maxEuWidget != null) ((TextWidget<?>) maxEuWidget).value(maxEu);
            else flow.child(maxEu.asWidget().name("max_eu"));

            var euText = Text
                    .lang(io == IO.IN ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                            FormattingUtil.formatNumber2Places(minAmperage), GTValues.VN[minVoltageTier])
                    .withStyle(ChatFormatting.UNDERLINE);

            RichTooltip tooltip = new RichTooltip();
            tooltip.addLine(Text.lang("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(eu.getTotalEU()))
                    .withStyle(ChatFormatting.UNDERLINE));

            if (euWidget != null) ((TextWidget<?>) euWidget).value(euText).tooltip(tooltip);
            else flow.child(euText.asWidget().tooltip(tooltip).marginBottom(1).name("eu"));
        }

    };
}
