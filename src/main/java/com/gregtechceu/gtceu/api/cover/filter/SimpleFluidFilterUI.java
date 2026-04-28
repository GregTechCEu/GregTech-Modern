package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ScrollablePhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class SimpleFluidFilterUI {

    private SimpleFluidFilterUI() {}

    static Object openConfigurator(SimpleFluidFilter filter, int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        filter.fluidStorageSlots = new CustomFluidTank[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;

                filter.fluidStorageSlots[index] = new CustomFluidTank(filter.maxStackSize);
                filter.fluidStorageSlots[index].setFluid(filter.matches[index]);

                var tank = new ScrollablePhantomFluidWidget(filter.fluidStorageSlots[index], 0, i * 18, j * 18, 18,
                        18,
                        () -> filter.fluidStorageSlots[index].getFluid(),
                        fluid -> filter.fluidStorageSlots[index].setFluid(fluid)) {

                    @Override
                    public void updateScreen() {
                        super.updateScreen();
                        setShowAmount(filter.maxStackSize > 1L);
                    }

                    @Override
                    public void detectAndSendChanges() {
                        super.detectAndSendChanges();
                        setShowAmount(filter.maxStackSize > 1L);
                    }
                };

                tank.setChangeListener(() -> {
                    filter.matches[index] = filter.fluidStorageSlots[index].getFluidInTank(0);
                    filter.onUpdated.accept(filter);
                }).setBackground(GuiTextures.SLOT);

                group.addWidget(tank);
            }
        }
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 0, 20, 20,
                GuiTextures.BUTTON_BLACKLIST, filter::isBlackList, filter::setBlackList));
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 20, 20, 20,
                GuiTextures.BUTTON_FILTER_NBT, filter::isIgnoreNbt, filter::setIgnoreNbt));
        return group;
    }
}
