package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.TreeUtil;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import java.util.Objects;
import java.util.Set;

public class GTRecipeViewerWidget extends ParentWidget<GTRecipeViewerWidget> {

    private final GTRecipe recipe;
    private final GTRecipeType recipeType;
    private final GTRecipeTypeUILayout uiLayout;

    public final Flow textComponents = Flow.col().coverChildren().leftRel(0).marginLeft(5);
    public final Flow inputColumn = Flow.col().coverChildren();
    public final Flow outputColumn = Flow.col().coverChildren();
    public final Flow recipeContentRow;
    public final ParentWidget<?> additionalRecipeContent = new ParentWidget<>()
            .coverChildrenHeight().widthRel(1f);

    private final int minTier;
    private int tier;

    public GTRecipeViewerWidget(GTRecipe recipe) {
        this.recipe = recipe;
        this.recipeType = recipe.getType();

        uiLayout = Objects.requireNonNull(recipe.getType().getUiLayout());

        minTier = RecipeHelper.getRecipeEUtTier(recipe);
        tier = minTier;

        Flow mainColumn = Flow.col().sizeRel(1f);

        child(mainColumn);

        coverChildrenWidth(134);
        coverChildrenHeight();

        recipeContentRow = uiLayout.getCustomUIBuilder() == null ? buildDefaultLayout() : uiLayout.getCustomUIBuilder().apply(recipe);
        mainColumn.child(recipeContentRow.marginTop(5));

        loadContentIntoSlots();

        mainColumn.child(additionalRecipeContent.child(textComponents).marginTop(5));
        buildAdditionalRecipeContent();
    }

    private Flow buildDefaultLayout() {
        var row = Flow.row()
        .horizontalCenter()
        .coverChildren()
        .childPadding((uiLayout.getProgressSize() / 2) + 2)
        .child(inputColumn)
        .child(uiLayout.getProgressWidgetSupplier().get(uiLayout, DoubleValue.simulateProgress(2000)))
        .child(outputColumn);

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

    public static String capabilityWidgetName(RecipeCapability<?> cap, IO io, int index) {
        return "%s_%s_%s".formatted(cap.name, io.toString(), index);
    }

    private void loadContentIntoSlots() {

        for (var cap: recipe.inputs.keySet()) {
            var content = recipe.inputs.get(cap);
            var widgetBuilder = uiLayout.capabilityInfo(cap).capabilityWidgetBuilder;
            if (widgetBuilder == null) continue;;

            for (int i=0; i<content.size();i++) {
                IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, IO.IN, i));
                if (widget == null) continue;
                widgetBuilder.buildWidgetContent(widget, i, content.get(i), IO.IN, recipeType, recipe, tier, tier);
            }
        }

        for (var cap: recipe.outputs.keySet()) {
            var content = recipe.outputs.get(cap);
            var widgetBuilder = uiLayout.capabilityInfo(cap).capabilityWidgetBuilder;
            if (widgetBuilder == null) continue;;

            for (int i=0; i<content.size();i++) {
                IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, IO.OUT, i));
                if (widget == null) continue;
                widgetBuilder.buildWidgetContent(widget, i, content.get(i), IO.OUT, recipeType, recipe, tier, tier);
            }
        }
    }

    private void buildAdditionalRecipeContent() {

        for (var condition: recipe.conditions) {
            condition.modifyUI().buildRecipeUI(recipe, this);
        }
    }

}
