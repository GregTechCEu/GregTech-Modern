package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class SimpleItemFilterUI {

    private SimpleItemFilterUI() {}

    static Object openConfigurator(SimpleItemFilter filter, int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;

                var handler = new CustomItemStackHandler(filter.matches[index]);

                var slot = new PhantomSlotWidget(handler, 0, i * 18, j * 18) {

                    @Override
                    public void updateScreen() {
                        super.updateScreen();
                        setMaxStackSize(filter.maxStackSize);
                    }

                    @Override
                    public void detectAndSendChanges() {
                        super.detectAndSendChanges();
                        setMaxStackSize(filter.maxStackSize);
                    }
                };

                slot.setChangeListener(() -> {
                    filter.matches[index] = handler.getStackInSlot(0);
                    filter.onUpdated.accept(filter);
                }).setBackground(GuiTextures.SLOT);

                group.addWidget(slot);
            }
        }
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 0, 20, 20,
                GuiTextures.BUTTON_BLACKLIST, filter::isBlackList, filter::setBlackList));
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 20, 20, 20,
                GuiTextures.BUTTON_FILTER_NBT, filter::isIgnoreNbt, filter::setIgnoreNbt));
        return group;
    }
}
