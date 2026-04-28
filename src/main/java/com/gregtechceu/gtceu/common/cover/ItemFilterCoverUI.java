package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class ItemFilterCoverUI {

    private ItemFilterCoverUI() {}

    static Object createUIWidget(ItemFilterCover cover) {
        final var group = new WidgetGroup(0, 0, 178, 85);
        group.addWidget(new LabelWidget(60, 5, cover.getAttachItem().getItem().getDescriptionId()));
        group.addWidget(new EnumSelectorWidget<>(35, 25, 18, 18,
                FilterMode.VALUES, cover.filterMode, cover::setFilterMode, FilterMode::getTooltip,
                CoverModeTextures::getFilterModeIcon));
        group.addWidget(new EnumSelectorWidget<>(35, 45, 18, 18, ManualIOMode.VALUES, cover.allowFlow,
                cover::setAllowFlow,
                ManualIOMode::getTooltip, CoverModeTextures::getManualIOModeIcon));
        group.addWidget((Widget) cover.getItemFilter().openConfigurator(62, 25));
        return group;
    }
}
