package com.gregtechceu.gtceu.common.mui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

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
                .size(150, 60)
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

}
