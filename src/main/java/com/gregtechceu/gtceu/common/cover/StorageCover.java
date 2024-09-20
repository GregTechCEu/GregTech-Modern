package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

public class StorageCover extends CoverBehavior implements IUICover {

    @Persisted
    @DescSynced
    public final ItemStackTransfer inventory;
    private int inventorysize = 18;
    private int xoffset = 7;
    private int yoffset = 7;

    public StorageCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
        inventory = new ItemStackTransfer(inventorysize) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 85);
        for (int slot = 0; slot < 18; slot++) {

            group.addWidget(new SlotWidget(inventory, slot, xoffset + (slot % 9) * 18, yoffset + (slot / 9) * 18));

        }

        return group;
    }
}
