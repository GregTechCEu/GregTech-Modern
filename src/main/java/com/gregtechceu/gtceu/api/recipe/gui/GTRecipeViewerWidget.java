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
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
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
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.ULV;
import static com.gregtechceu.gtceu.api.GTValues.V;

public class GTRecipeViewerWidget extends ParentWidget<GTRecipeViewerWidget> {

    private final GTRecipe recipe;
    private final GTRecipeType recipeType;
    private final GTRecipeTypeUILayout uiLayout;

    public final ListWidget<IWidget, ?> textComponents = new ListWidget<>()
            .widthRel(1f)
            .coverChildrenHeight()
            .crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow inputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow outputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow recipeContentRow;
    public final ParentWidget<?> additionalRecipeContent = new ParentWidget<>()
            .coverChildrenHeight().widthRel(1f);

    private final int minTier;
    private int tier;

    private int duration;
    private long euTotal;
    private EnergyStack EUt;

    public GTRecipeViewerWidget(GTRecipe recipe) {
        this.recipe = recipe;
        this.recipeType = recipe.getType();

        uiLayout = Objects.requireNonNull(recipe.getType().getUiLayout(), "No recipe type UI declared, add one to your recipe type definition.");

        minTier = RecipeHelper.getRecipeEUtTier(recipe);
        tier = minTier;
        duration = recipe.duration;
        euTotal = RecipeHelper.getRealEUtWithIO(recipe).getTotalEU()*duration;
        EUt = RecipeHelper.getRealEUt(recipe);
        boolean isEnergyIn = RecipeHelper.getRealEUtWithIO(recipe).isInput();
        Flow mainColumn = Flow.col().widthRel(1f).coverChildrenHeight();

        child(mainColumn);
        padding(3);
        coverChildrenWidth(134);
        coverChildrenHeight(60);

        // Attach duration here so it is always the first text row
        textComponents.childIf(!recipe.data.getBoolean("hide_duration"),
                () -> Text.dynamic(() -> Component.translatable("gtceu.recipe.duration", FormattingUtil.formatNumbers((double)duration /20))).asWidget());

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
            if (widgetBuilder == null) continue;

            for (int i=0; i<content.size();i++) {
                IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, IO.IN, i));
                if (widget == null) continue;
                widgetBuilder.buildWidgetContent(widget, i, content.get(i), IO.IN, recipeType, recipe, tier, tier);
            }
        }

        for (var cap: recipe.outputs.keySet()) {
            var content = recipe.outputs.get(cap);
            var widgetBuilder = uiLayout.capabilityInfo(cap).capabilityWidgetBuilder;
            if (widgetBuilder == null) continue;

            for (int i=0; i<content.size();i++) {
                IWidget widget = WidgetTree.findFirstWithNameNullable(this, capabilityWidgetName(cap, IO.OUT, i));
                if (widget == null) continue;
                widgetBuilder.buildWidgetContent(widget, i, content.get(i), IO.OUT, recipeType, recipe, tier, tier);
            }
        }
    }

    private void buildAdditionalRecipeContent() {

        var eu = RecipeHelper.getRealEUtWithIO(recipe);

        if (eu.voltage() > 0) {
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.data.getBoolean("duration_is_total_cwu") &&
                    recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.max(recipe.tickInputs.get(CWURecipeCapability.CAP).stream()
                        .map(Content::content).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                textComponents.child(Text.dynamic(() ->
                        Component.translatable("gtceu.recipe.max_eu",
                        FormattingUtil.formatNumbers(euTotal / minimumCWUt))).asWidget());
            } else {
                textComponents.child(Text.dynamic(() -> Component.translatable("gtceu.recipe.total", FormattingUtil.formatNumbers(euTotal))).asWidget());
            }

            textComponents.child(Text.dynamic(() -> {
                var minVoltageTier = GTUtil.getTierByVoltage(EUt.voltage());
                float minAmperage = (float) EUt.getTotalEU() / GTValues.V[minVoltageTier];
                return Component.translatable(eu.isInput() ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                        FormattingUtil.formatNumber2Places(minAmperage), GTValues.VN[minVoltageTier]).withStyle(ChatFormatting.UNDERLINE);
            }).asWidget().tooltip(
                    r -> r.addLine(Text.dynamic(() -> Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(EUt.getTotalEU()))
                    .withStyle(ChatFormatting.UNDERLINE)))));
        }

        if (recipe.tickInputs.get(CWURecipeCapability.CAP) != null) {
            if (CWURecipeCapability.CAP.isTickSlot(0, IO.IN, recipe)) {
                int cwu = recipe.getTickInputContents(CWURecipeCapability.CAP).stream().map(Content::content).mapToInt(CWURecipeCapability.CAP::of).sum();
                textComponents.child(Text.lang("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu)).asWidget());
            }
            if (recipe.data.getBoolean("duration_is_total_cwu")) {
                textComponents.child(Text.lang("gtceu.recipe.total_computation", FormattingUtil.formatNumbers(recipe.duration)).asWidget());
            }
        }


        for (var condition: recipe.conditions) {
            condition.modifyUI().buildRecipeUI(recipe, this);
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
                    if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
                        oc = FusionReactorMachine.FUSION_OC;
                    }

                    applyOverclock(oc);
                    return true;
                });
    }

    private void applyOverclock(OverclockingLogic logic) {
        EnergyStack inputEUt = recipe.getInputEUt();

        if (tier > minTier && !inputEUt.isEmpty()) {
            int ocs = tier - minTier;
            if (minTier == ULV) ocs--;
            var params = new OverclockingLogic.OCParams(inputEUt.voltage(), recipe.duration, ocs, 1);
            var result = logic.runOverclockingLogic(params, V[tier]);
            duration = (int) (recipe.duration * result.durationMultiplier());
            EUt = inputEUt.multiplyVoltage(result.eutMultiplier());
        } else {
            duration = recipe.duration;
            EUt = RecipeHelper.getRealEUt(recipe);
        }
        euTotal = EUt.getTotalEU()*duration;

    }

}
