package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.ULV;
import static com.gregtechceu.gtceu.api.GTValues.V;

public class GTRecipeViewerWidget extends ParentWidget<GTRecipeViewerWidget> {

    private final GTRecipe baseRecipe;

    private GTRecipe modifiedRecipe;

    private final GTRecipeType recipeType;
    private final GTRecipeTypeUILayout uiLayout;

    public final ListWidget<IWidget, ?> textComponents = new ListWidget<>()
            .widthRel(1f)
            .coverChildrenHeight()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .collapseDisabledChildren();
    public final Flow inputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow outputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow recipeContentRow;
    public final ParentWidget<?> additionalRecipeContent = new ParentWidget<>()
            .coverChildrenHeight().widthRel(1f);

    private final int minTier;
    private int tier;

    public GTRecipeViewerWidget(GTRecipe recipe) {
        this.baseRecipe = recipe;
        this.recipeType = recipe.getType();

        modifiedRecipe = recipe;

        uiLayout = Objects.requireNonNull(recipe.getType().getUiLayout(), "No recipe type UI declared, add one to your recipe type definition.");

        minTier = RecipeHelper.getRecipeEUtTier(recipe);
        tier = minTier;
        boolean isEnergyIn = RecipeHelper.getRealEUtWithIO(recipe).isInput();
        Flow mainColumn = Flow.col().widthRel(1f).coverChildrenHeight();

        child(mainColumn);
        padding(3);
        coverChildrenWidth(134);
        coverChildrenHeight(60);

        // Attach duration here so it is always the first text row
        textComponents.child(Text.dynamic(() -> Component.translatable("gtceu.recipe.duration", FormattingUtil.formatNumbers((double)modifiedRecipe.duration/20)))
                .asWidget()
                .setEnabledIf(v -> !modifiedRecipe.data.getBoolean("hide_duration"))
        );

        recipeContentRow = uiLayout.getCustomUIBuilder() == null ? buildDefaultLayout() : uiLayout.getCustomUIBuilder().apply(recipe);
        mainColumn.child(recipeContentRow.marginTop(5));

        loadContentIntoSlots();

        mainColumn.child(additionalRecipeContent.child(textComponents));
        buildAdditionalRecipeContent();

        childIf(isEnergyIn, this::buildOverclockButton);

        childIf(!FMLLoader.isProduction(), () -> new ButtonWidget<>()
                .overlay(Text.str("ID"))
                .decoration()
                .top(3).right(3)
                .size(15, 15)
                .tooltip(r -> r.addLine("Click to copy recipe ID: " + recipe.id))
                .onMousePressed((ctx, b) -> {
                    Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString());
                    return true;
                })
        );
    }

    private Flow buildDefaultLayout() {
        var row = Flow.row()
        .horizontalCenter()
        .coverChildren()
        .margin(10, 10, 0, 0)
        .childPadding((uiLayout.getProgressSize() / 2) + 2)
        .child(inputColumn)
        .child(uiLayout.getProgressWidgetSupplier().get(uiLayout, DoubleValue.simulateProgress(2000)))
        .child(outputColumn);

        for (var entry: recipeType.maxInputs.object2IntEntrySet()) {
            var layoutFunc = uiLayout.capabilityInfo(entry.getKey()).recipeViewerLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(uiLayout, this, IO.IN);
        }

        for (var entry: recipeType.maxOutputs.object2IntEntrySet()) {
            var layoutFunc = uiLayout.capabilityInfo(entry.getKey()).recipeViewerLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(uiLayout, this, IO.OUT);
        }
        return row;
    }

    public static String capabilityWidgetName(RecipeCapability<?> cap, IO io, int index) {
        return "%s_%s_%s".formatted(cap.name, io.toString().toLowerCase(), index);
    }

    private void loadContentIntoSlots() {
        var allInputCaps = new HashSet<>(modifiedRecipe.inputs.keySet());
        allInputCaps.addAll(modifiedRecipe.tickInputs.keySet());
        var allOutputCaps = new HashSet<>(modifiedRecipe.outputs.keySet());
        allOutputCaps.addAll(modifiedRecipe.tickOutputs.keySet());

        for (var cap: allInputCaps) {
            loadCapContent(cap, IO.IN);
        }

        for (var cap: allOutputCaps) {
            loadCapContent(cap, IO.OUT);
        }
    }

    private void loadCapContent(RecipeCapability<?> cap, IO io) {
        List<Content> contents = io == IO.IN ? modifiedRecipe.getInputContents(cap) : modifiedRecipe.getOutputContents(cap);
        List<Content> tickContents = io == IO.IN ? modifiedRecipe.getTickInputContents(cap) : modifiedRecipe.getTickOutputContents(cap);

        var widgetBuilder = uiLayout.capabilityInfo(cap).capabilityWidgetBuilder;
        if (widgetBuilder == null) return;

        int currentContentIndex = 0;

        for (var content: contents) {
            IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, io, currentContentIndex));
            if (widget == null) continue;
            widgetBuilder.buildWidgetContent(widget, content, io, false, recipeType, modifiedRecipe, tier, tier);
            currentContentIndex++;
        }

        for (var tickContent: tickContents) {
            IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, io, currentContentIndex));
            if (widget == null) continue;
            widgetBuilder.buildWidgetContent(widget, tickContent, io, true, recipeType, modifiedRecipe, tier, tier);
            currentContentIndex++;
        }
    }

    private void buildAdditionalRecipeContent() {
        for (var condition: baseRecipe.conditions) {
            condition.modifyUI().buildRecipeUI(baseRecipe, this);
        }
    }

    private ButtonWidget<?> buildOverclockButton() {
        return new ButtonWidget<>().background(IDrawable.NONE)
                //.hoverBackground(IDrawable.NONE)
                .size(25, 15)
                .bottom(0).right(5)
                .decoration()
                .overlay(Text.dynamic(() -> Component.literal(GTValues.VNF[tier])))
                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang("gtceu.oc.tooltip", GTValues.VNF[minTier])))
                .onMousePressed((ctx, b) -> {
                    var mouse = MouseData.create(b);

                    OverclockingLogic oc = OverclockingLogic.NON_PERFECT_OVERCLOCK;

                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        if (tier == GTValues.MAX) return true;
                        tier++;
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        if (tier == minTier) return true;
                        tier--;
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        tier = minTier;
                    }

                    if (mouse.shift()) oc = OverclockingLogic.PERFECT_OVERCLOCK;
                    if (modifiedRecipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
                        oc = FusionReactorMachine.FUSION_OC;
                    }

                    applyOverclock(oc);
                    return true;
                });
    }

    private void applyOverclock(OverclockingLogic logic) {
        EnergyStack inputEUt = baseRecipe.getInputEUt();

        if (tier > minTier && !inputEUt.isEmpty()) {
            int ocs = tier - minTier;
            if (minTier == ULV) ocs--;
            var params = new OverclockingLogic.OCParams(inputEUt.voltage(), modifiedRecipe.duration, ocs, 1);
            var modifier = logic.runOverclockingLogic(params, V[tier]).toModifier();

            modifiedRecipe = Objects.requireNonNull(modifier.apply(baseRecipe));
        } else {
            modifiedRecipe = baseRecipe;
        }

        loadContentIntoSlots();
    }

}
