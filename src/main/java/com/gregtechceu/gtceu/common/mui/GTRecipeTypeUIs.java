package com.gregtechceu.gtceu.common.mui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.Objects;

public class GTRecipeTypeUIs {

    public static final GTRecipeTypeUILayout.CustomRecipeUIBuilder RESEARCH_STATION = (recipeWidget, uiLayout, recipe) -> {

        var inputSlot = RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, GTGuiTextures.SCANNER_OVERLAY)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, 0));

        var dataInputSlot = RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, GTGuiTextures.SCANNER_OVERLAY)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, 1));

        var outputSlot = RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, GTGuiTextures.RESEARCH_STATION_OVERLAY)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.OUT, 0));

        var inputSlotBackgroundParent = new ParentWidget<>()
                .size(84, 60)
                .background(GTGuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE)
                .child(dataInputSlot.center());

        var doubleValue = DoubleValue.simulateProgress(2000);

        var progressWidget1 = new ProgressWidget().size(54, 5)
                .texture(GTGuiTextures.PROGRESS_BAR_RESEARCH_STATION_1, ProgressDrawable.Direction.RIGHT)
                .clientValue(() -> Mth.clamp(doubleValue.getDoubleValue(), 0, 0.8)/0.8);

        var progressWidget2 = new ProgressWidget().size(10, 18)
                .texture(GTGuiTextures.PROGRESS_BAR_RESEARCH_STATION_2, ProgressDrawable.Direction.DOWN)
                .clientValue(() -> Mth.clamp(doubleValue.getDoubleValue()-0.8, 0, 0.2)/0.2);

        var mainWidget = new ParentWidget<>()
                .size(125, 60)
                .child(inputSlotBackgroundParent)
                .child(progressWidget1.pos(62, 28))
                .child(inputSlot.pos(87, 21))
                .child(progressWidget2.pos(109, 31))
                .child(outputSlot.pos(105, 49));

        // Add the EU and computation EU since it should still be displayed

        var euLayoutFunc = uiLayout.capabilityInfo(EURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        var compLayoutFunc = uiLayout.capabilityInfo(CWURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        return mainWidget;
    };

    public static final GTRecipeTypeUILayout.CustomRecipeUIBuilder DISTILLATION_TOWER = (recipeWidget, uiLayout, recipe) -> {

        var inputSlot = RecipeViewerSlotWidget.create(FluidStack.class).value(FluidStackList.of(FluidStack.EMPTY))
                .background(GuiTextures.SLOT_FLUID, GTGuiTextures.BEAKER_OVERLAY_1)
                .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, IO.IN, 0));

        var outputs = SlotGroupWidget
                .builder()
                .matrix("sss",
                        "sss",
                        "sss",
                        "sss", "S")
                .key('s', i -> {
                    int row = 3 - i/3;
                    int index = i % 3;
                    return RecipeViewerSlotWidget.create(FluidStack.class).value(FluidStackList.of(FluidStack.EMPTY))
                            .background(GuiTextures.SLOT_FLUID, GTGuiTextures.BEAKER_OVERLAY_3)
                            .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, IO.OUT, row*3 + index));
                })
                .key('S', i -> RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                        .background(GuiTextures.SLOT_ITEM, GTGuiTextures.DUST_OVERLAY)
                        .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.OUT, i)))
                .build()
                .coverChildren(18, 18);

        var doubleValue = DoubleValue.simulateProgress(2000);

        var progressWidget = new ProgressWidget().size(65, 75)
                .texture(GTGuiTextures.PROGRESS_BAR_DISTILLATION_TOWER, ProgressDrawable.Direction.RIGHT)
                .value(doubleValue);

        var progressWidgetBubbles = new ProgressWidget().size(7, 31)
                .texture(GTGuiTextures.PROGRESS_BAR_DISTILLATION_BUBBLES, ProgressDrawable.Direction.UP)
                .clientValue(() -> Mth.clamp(doubleValue.getDoubleValue()-0.3, 0, 0.7)/0.7);

        var progressWidgetCoil = new ProgressWidget().size(12, 20)
                .texture(GTGuiTextures.PROGRESS_BAR_DISTILLATION_COIL, ProgressDrawable.Direction.UP)
                .clientValue(() -> Mth.clamp(doubleValue.getDoubleValue(), 0, 0.3)/0.3);

        var parentWidget = new ParentWidget<>()
                .size(130, 90)
                .child(progressWidgetBubbles.pos(16, 2))
                .child(inputSlot.pos(10, 35))
                .child(progressWidgetCoil.pos(13, 54))
                .child(progressWidget.pos(28, 8))
                .child(outputs.pos(69, 0));


        // Add the EU and computation EU since it should still be displayed

        var euLayoutFunc = uiLayout.capabilityInfo(EURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        var compLayoutFunc = uiLayout.capabilityInfo(CWURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        return parentWidget;
    };

    public static final GTRecipeTypeUILayout.CustomRecipeUIBuilder ASSLINE = (recipeWidget, uiLayout, recipe) -> {

        var itemInputs = SlotGroupWidget
                .builder()
                .matrix("ssss", "ssss", "ssss", "ssss")
                .key('s', i -> RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                        .background(GuiTextures.SLOT_ITEM)
                        .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, i)))
                .build()
                .coverChildren(18, 18);

        var fluidSlots = SlotGroupWidget
                .builder()
                .matrix("s", "s", "s", "s")
                .key('s', i -> RecipeViewerSlotWidget.create(FluidStack.class).value(FluidStackList.of(FluidStack.EMPTY))
                        .background(GuiTextures.SLOT_FLUID)
                        .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, IO.IN, i)))
                .build()
                .coverChildren(18, 18);

        var outputSlot = RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, GTGuiTextures.RESEARCH_STATION_OVERLAY)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.OUT, 0));

        RecipeViewerSlotWidget<ItemStack, ?> researchSlot = null;

        for (var condition : recipe.conditions) {
            if (condition instanceof ResearchCondition researchCondition) {
                for (var researchItem : researchCondition.getData()) {
                    researchSlot = RecipeViewerSlotWidget.create(ItemStack.class)
                            .recipeSlotRole(RecipeSlotRole.CATALYST)
                            .value(researchItem.getDataItem());
                }
            }
        }

        var doubleValue = DoubleValue.simulateProgress(2000);

        var progressWidget1 = new ProgressWidget().size(54, 72)
                .texture(GTGuiTextures.PROGRESS_BAR_ASSEMBLY_LINE, ProgressDrawable.Direction.RIGHT)
                .value(doubleValue);

        var progressWidget2 = new ProgressWidget().size(10, 18)
                .texture(GTGuiTextures.PROGRESS_BAR_ASSEMBLY_LINE_ARROW, ProgressDrawable.Direction.UP)
                .value(doubleValue);

        var parentWidget = new ParentWidget<>()
                .size(144, 72)
                .child(itemInputs)
                .child(progressWidget1.pos(72, 0))
                .child(fluidSlots.pos(90, 0))
                .child(outputSlot.pos(126, 0));

        if (researchSlot != null) {
            parentWidget.child(progressWidget2.pos(130, 18));
            parentWidget.child(researchSlot.pos(126, 36));
        }

        // Add the EU and computation EU since it should still be displayed

        var euLayoutFunc = uiLayout.capabilityInfo(EURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(euLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        var compLayoutFunc = uiLayout.capabilityInfo(CWURecipeCapability.CAP).recipeViewerLayoutBuilder;
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.IN);
        Objects.requireNonNull(compLayoutFunc).createCapabilityUILayout(uiLayout, recipeWidget, IO.OUT);

        return parentWidget;
    };

}
