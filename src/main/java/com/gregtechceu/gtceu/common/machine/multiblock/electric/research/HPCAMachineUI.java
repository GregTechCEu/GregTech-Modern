package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ExtendedProgressWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.level.Level;

import java.util.function.Supplier;

final class HPCAMachineUI {

    private HPCAMachineUI() {}

    static Object createUIWidget(HPCAMachine machine, Object superWidget) {
        WidgetGroup builder = (WidgetGroup) superWidget;
        // Create the hover grid.
        builder.addWidget(new ExtendedProgressWidget(
                () -> machine.hpcaHandler.getAllocatedCWUt() > 0 ? machine.progressSupplier.getAsDouble() : 0,
                74, 57, 47, 47, GuiTextures.HPCA_COMPONENT_OUTLINE)
                .setServerTooltipSupplier(machine.hpcaHandler::addInfo)
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT));
        int startX = 76;
        int startY = 59;

        // We need to know what components we have on the client.
        Level level = machine.getLevel();
        if (level != null && level.isClientSide()) {
            if (machine.isFormed()) {
                machine.hpcaHandler.tryGatherClientComponents(level, machine.getBlockPos(), machine.getFrontFacing(),
                        machine.getUpwardsFacing(), machine.isFlipped());
            } else {
                machine.hpcaHandler.clearClientComponents();
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;
                Supplier<IGuiTexture> textureSupplier = () -> getComponentTexture(machine.hpcaHandler
                        .getComponentIcon(index));
                builder.addWidget(new ImageWidget(startX + (15 * j), startY + (15 * i), 13, 13, textureSupplier));
            }
        }
        return builder;
    }

    private static IGuiTexture getComponentTexture(Object icon) {
        return icon instanceof IGuiTexture texture ? texture : GuiTextures.BLANK_TRANSPARENT;
    }
}
