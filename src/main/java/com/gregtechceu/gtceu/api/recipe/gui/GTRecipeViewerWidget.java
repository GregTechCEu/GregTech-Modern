package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.value.DoubleValue;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import java.util.Objects;

public class GTRecipeViewerWidget extends ParentWidget<GTRecipeViewerWidget> {

    private final GTRecipe recipe;
    private final GTRecipeType recipeType;
    private final GTRecipeTypeUILayout uiLayout;

    public final Flow textComponents;

    public final Flow inputColumn = Flow.col().coverChildren();
    public final Flow outputColumn = Flow.col().coverChildren();

    public final Flow recipeContentRow;

    public GTRecipeViewerWidget(GTRecipe recipe) {
        this.recipe = recipe;
        this.recipeType = recipe.getType();
        uiLayout = Objects.requireNonNull(recipe.getType().getUiLayout());
        textComponents = Flow.col();

        coverChildrenHeight();

        recipeContentRow = uiLayout.getCustomUIBuilder() == null ? buildDefaultLayout() : uiLayout.getCustomUIBuilder().apply(recipe);
        child(recipeContentRow);

        loadContentIntoSlots();


    }

    private Flow buildDefaultLayout() {
        var row = Flow.row();
        row.coverChildren();
        row.childPadding((uiLayout.getProgressSize() / 2) + 2);
        row.child(inputColumn);
        row.child(uiLayout.getProgressWidgetSupplier().get(uiLayout, DoubleValue.simulateProgress(2000)));
        row.child(outputColumn);

        for (var entry: recipeType.maxInputs.object2IntEntrySet()) {
            var layoutFunc = uiLayout.capabilityInfo(entry.getKey()).recipeViewerLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(recipe, uiLayout, this, IO.IN);
        }

        for (var entry: recipeType.maxOutputs.object2IntEntrySet()) {
            var layoutFunc = uiLayout.capabilityInfo(entry.getKey()).recipeViewerLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(recipe, uiLayout, this, IO.OUT);
        }
        return row;
    }

    private void loadContentIntoSlots() {

    }

}
