package com.gregtechceu.gtceu.integration.forge;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.forge.GasRecipeCapability;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.forge.GasWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class GTRecipeWidgetImpl {
    public static void collectExtraStorage(Table<IO, RecipeCapability<?>, Object> extraTable, Table<IO, RecipeCapability<?>, List<Content>> extraContents, GTRecipe recipe) {
        if (GTCEu.isMekanismLoaded()) {
            IGasTank[] inputGases = new IGasTank[recipe.recipeType.getMaxInputs(GasRecipeCapability.CAP)];
            List<Content> inputGasContents = new ArrayList<>(recipe.getInputContents(GasRecipeCapability.CAP));
            inputGasContents.addAll(recipe.getTickInputContents(GasRecipeCapability.CAP));
            for (int i = 0; i < inputGasContents.size(); i++) {
                inputGases[i] = new SingleGasTank(GasRecipeCapability.CAP.of(inputGasContents.get(i).content));
            }
            extraTable.put(IO.IN, GasRecipeCapability.CAP, inputGases);
            extraContents.put(IO.IN, GasRecipeCapability.CAP, inputGasContents);

            IGasTank[] outputGases = new IGasTank[recipe.recipeType.getMaxOutputs(GasRecipeCapability.CAP)];
            List<Content> outputGasContents = new ArrayList<>(recipe.getOutputContents(GasRecipeCapability.CAP));
            outputGasContents.addAll(recipe.getTickOutputContents(GasRecipeCapability.CAP));
            for (int i = 0; i < outputGasContents.size(); i++) {
                outputGases[i] = new SingleGasTank(GasRecipeCapability.CAP.of(outputGasContents.get(i).content));
            }
            extraTable.put(IO.OUT, GasRecipeCapability.CAP, outputGases);
        }
    }

    public static void renderExtras(GTRecipe recipe, WidgetGroup group, Table<IO, RecipeCapability<?>, List<Content>> extraContents) {
        if (GTCEu.isMekanismLoaded()) {
            var inputGas = extraContents.get(IO.IN, GasRecipeCapability.CAP);
            var outputGas = extraContents.get(IO.OUT, GasRecipeCapability.CAP);

            // bind gas in overlay
            WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(GasRecipeCapability.CAP.slotName(IO.IN)), GasWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < inputGas.size()) {
                    var content = inputGas.get(index);
                    tank.setOverlay(content.createOverlay(index >= recipe.getInputContents(FluidRecipeCapability.CAP).size()));
                    tank.setOnAddedTooltips((w, tooltips) -> {
                        var chance = content.chance;
                        if (chance < 1) {
                            tooltips.add(chance == 0 ?
                                    Component.translatable("gtceu.gui.content.chance_0") :
                                    Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", chance * 100) + "%"));
                            if (content.tierChanceBoost > 0) {
                                tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", content.tierChanceBoost * 100) + "%"));
                            }
                        }
                        if (index >= recipe.getInputContents(GasRecipeCapability.CAP).size()) {
                            tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                        }
                    });
                }
            });
            // bind gas out overlay
            WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(GasRecipeCapability.CAP.slotName(IO.OUT)), GasWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < outputGas.size()) {
                    var content = outputGas.get(index);
                    tank.setOverlay(content.createOverlay(index >= recipe.getOutputContents(GasRecipeCapability.CAP).size()));
                    tank.setOnAddedTooltips((w, tooltips) -> {
                        var chance = content.chance;
                        if (chance < 1) {
                            tooltips.add(chance == 0 ?
                                    Component.translatable("gtceu.gui.content.chance_0") :
                                    Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", chance * 100) + "%"));
                            if (content.tierChanceBoost > 0) {
                                tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", content.tierChanceBoost * 100) + "%"));
                            }
                        }
                        if (index >= recipe.getOutputContents(GasRecipeCapability.CAP).size()) {
                            tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                        }
                    });
                }
            });
        }
    }

    public static class SingleGasTank implements IGasTank {

        private GasStack stack;

        public SingleGasTank(GasStack stack) {
            this.stack = stack;
        }

        @Override
        public GasStack getStack() {
            return this.stack;
        }

        @Override
        public void setStack(GasStack stack) {
            this.stack = stack;
        }

        @Override
        public void setStackUnchecked(GasStack stack) {
            this.stack = stack;
        }

        @Override
        public long getCapacity() {
            return stack.getAmount();
        }

        @Override
        public boolean isValid(GasStack stack) {
            return true;
        }

        @Override
        public void onContentsChanged() {

        }
    }
}
