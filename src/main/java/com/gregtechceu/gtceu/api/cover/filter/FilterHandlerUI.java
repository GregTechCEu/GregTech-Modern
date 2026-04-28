package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

final class FilterHandlerUI {

    private FilterHandlerUI() {}

    static Object createFilterSlotUI(FilterHandler<?, ?> handler, int xPos, int yPos) {
        return new SlotWidget(handler.getFilterSlot(), 0, xPos, yPos)
                .setChangeListener(handler::updateFilter)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
    }

    static Object createFilterConfigUI(FilterHandler<?, ?> handler, int xPos, int yPos, int width, int height) {
        WidgetGroup filterGroup = new WidgetGroup(xPos, yPos, width, height);
        if (!handler.getFilterItem().isEmpty()) {
            filterGroup.addWidget((Widget) handler.getFilter().openConfigurator(0, 0));
        }
        return filterGroup;
    }

    static void updateFilterGroupUI(Object filterGroup, ItemStack filterItem, @Nullable Filter<?, ?> filter) {
        WidgetGroup group = (WidgetGroup) filterGroup;
        group.clearAllWidgets();

        if (!filterItem.isEmpty() && filter != null) {
            group.addWidget((Widget) filter.openConfigurator(0, 0));
        }
    }
}
