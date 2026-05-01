package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

final class FluidHatchPartMachineUI {

    private FluidHatchPartMachineUI() {}

    static void attachConfigurators(FluidHatchPartMachine machine, ConfiguratorPanel configuratorPanel) {
        if (machine.isCircuitSlotEnabled() && machine.getIo().support(IO.IN)) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(machine.circuitInventory.storage));
        }
    }

    static Widget createUIWidget(FluidHatchPartMachine machine) {
        if (machine.slots == 1) {
            return createSingleSlotGUI(machine);
        }
        return createMultiSlotGUI(machine);
    }

    private static Widget createSingleSlotGUI(FluidHatchPartMachine machine) {
        var group = new WidgetGroup(0, 0, 89, 63);
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY));
        TankWidget tankWidget;

        if (machine.getIo().support(IO.OUT)) {
            tankWidget = new PhantomFluidWidget(machine.tank.getLockedFluid(), 0, 67, 40, 18, 18,
                    () -> machine.tank.getLockedFluid().getFluid(), f -> {
                        if (!machine.tank.getFluidInTank(0).isEmpty()) {
                            return;
                        }
                        if (f == null || f.isEmpty()) {
                            machine.tank.setLocked(false);
                        } else {
                            FluidStack newFluid = f.copy();
                            newFluid.setAmount(1);
                            machine.tank.setLocked(true, newFluid);
                        }
                    }).setShowAmount(false).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT);
            group.addWidget(tankWidget);

            group.addWidget(new ToggleButtonWidget(7, 40, 18, 18,
                    GuiTextures.BUTTON_LOCK, machine.tank::isLocked, machine.tank::setLocked)
                    .setTooltipText("gtceu.gui.fluid_lock.tooltip")
                    .setShouldUseBaseBackground())
                    .addWidget(new TankWidget(machine.tank.getStorages()[0], 67, 22, 18, 18, true,
                            machine.getIo().support(IO.IN))
                            .setShowAmount(true).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT));
        } else {
            tankWidget = new TankWidget(machine.tank.getStorages()[0], 67, 22, 18, 18, true,
                    machine.getIo().support(IO.IN))
                    .setShowAmount(true).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT);
            group.addWidget(tankWidget);
        }

        TankWidget displayedTank = tankWidget;
        group.addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> getFluidAmountText(machine, displayedTank)))
                .addWidget(new LabelWidget(8, 28, () -> getFluidNameText(machine, displayedTank).getString()));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private static Component getFluidNameText(FluidHatchPartMachine machine, TankWidget tankWidget) {
        if (!machine.tank.getFluidInTank(tankWidget.getTank()).isEmpty()) {
            return machine.tank.getFluidInTank(tankWidget.getTank()).getHoverName();
        }
        return machine.tank.getLockedFluid().getFluid().getHoverName();
    }

    private static String getFluidAmountText(FluidHatchPartMachine machine, TankWidget tankWidget) {
        if (!machine.tank.getFluidInTank(tankWidget.getTank()).isEmpty()) {
            return machine.getFormattedFluidAmount(machine.tank.getFluidInTank(tankWidget.getTank()));
        }
        if (!machine.tank.getLockedFluid().getFluid().isEmpty()) {
            return "0";
        }
        return "";
    }

    private static Widget createMultiSlotGUI(FluidHatchPartMachine machine) {
        int rowSize = (int) Math.sqrt(machine.slots);
        int colSize = rowSize;
        if (machine.slots == 8) {
            rowSize = 4;
            colSize = 2;
        }

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new TankWidget(machine.tank.getStorages()[index++], 4 + x * 18, 4 + y * 18, true,
                                machine.getIo().support(IO.IN))
                                .setBackground(GuiTextures.FLUID_SLOT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
