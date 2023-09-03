package com.gregtechceu.gtceu.api.recipe.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.forge.GasRecipeCapability;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.forge.GasWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.Position;
import mekanism.api.chemical.gas.IGasTank;

public class GTRecipeTypeImpl {
    public static void bindPlatformIO(WidgetGroup template, GTRecipeType.RecipeHolder recipeHolder, boolean isJEI) {

        if (GTCEu.isMekanismLoaded()) {
            // bind gas in
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(GasRecipeCapability.CAP.slotName(IO.IN)), GasWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < ((IGasTank[])recipeHolder.extraStorages().get(IO.IN, GasRecipeCapability.CAP)).length) {
                    tank.setGasTank(((IGasTank[])recipeHolder.extraStorages().get(IO.IN, GasRecipeCapability.CAP))[index]);
                    tank.setIngredientIO(IngredientIO.INPUT);
                    tank.setAllowClickFilled(!isJEI);
                    tank.setAllowClickDrained(!isJEI);
                }
            });
            // bind gas out
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(GasRecipeCapability.CAP.slotName(IO.OUT)), GasWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < ((IGasTank[])recipeHolder.extraStorages().get(IO.IN, GasRecipeCapability.CAP)).length) {
                    tank.setGasTank(((IGasTank[])recipeHolder.extraStorages().get(IO.OUT, GasRecipeCapability.CAP))[index]);
                    tank.setIngredientIO(IngredientIO.OUTPUT);
                    tank.setAllowClickFilled(!isJEI);
                    tank.setAllowClickDrained(false);
                }
            });
        }
    }

    public static void addPlatformSlots(GTRecipeType type, WidgetGroup group, boolean isOutputs, boolean isSteam, boolean isHighPressure, int index, int sum) {
        if (GTCEu.isMekanismLoaded()) {
            int gasCount = isOutputs ? type.getMaxOutputs(GasRecipeCapability.CAP) : type.getMaxInputs(GasRecipeCapability.CAP);
            for (int i = 0; i < gasCount; i++) {
                var gasTank = new GasWidget();
                gasTank.initTemplate();
                gasTank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
                gasTank.setSelfPosition(new Position((index % 3) * 18 + 4, (index / 3) * 18 + 4));
                gasTank.setBackground(type.getOverlaysForSlot(isOutputs, true, i == gasCount - 1, isSteam, isHighPressure));
                gasTank.setId(GasRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, i));
                group.addWidget(gasTank);
                index++;
            }
        }
    }
}
