package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

final class CWURecipeCapabilityUI {

    private CWURecipeCapabilityUI() {}

    static void addXEIInfo(Object group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           MutableInt yOffset) {
        WidgetGroup widgetGroup = (WidgetGroup) group;
        if (perTick) {
            int cwu = contents.stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum();
            widgetGroup.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu))));
        }
        if (recipe.data.getBooleanOr("duration_is_total_cwu", false)) {
            widgetGroup.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.total_computation",
                            FormattingUtil.formatNumbers(recipe.duration))));
        }
    }
}
