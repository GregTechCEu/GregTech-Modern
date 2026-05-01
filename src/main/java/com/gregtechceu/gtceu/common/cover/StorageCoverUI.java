package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.network.chat.Component;

final class StorageCoverUI {

    private StorageCoverUI() {}

    static Widget createUIWidget(StorageCover cover) {
        final var group = new WidgetGroup(0, 0, 126, 87);

        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(cover.getUITitle())));
        addSlots(cover, group);

        return group;
    }

    static IFancyConfigurator createConfigurator(StorageCover cover) {
        return new StorageCoverConfigurator(cover);
    }

    private static void addSlots(StorageCover cover, WidgetGroup group) {
        for (int slot = 0; slot < cover.inventory.getSlots(); slot++) {
            group.addWidget(new SlotWidget(cover.inventory, slot, 7 + (slot % 6) * 18, 21 + (slot / 6) * 18));
        }
    }

    private record StorageCoverConfigurator(StorageCover cover) implements IFancyConfigurator {

        @Override
        public Component getTitle() {
            return Component.translatable("cover.storage.title");
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.STORAGE_ICON;
        }

        @Override
        public Widget createConfigurator() {
            final var group = new WidgetGroup(0, 0, 126, 87);
            addSlots(cover, group);
            return group;
        }
    }
}
