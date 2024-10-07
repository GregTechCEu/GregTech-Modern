package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StorageCover extends CoverBehavior implements IUICover {

    @Persisted
    @DescSynced
    public final ItemStackTransfer inventory;
    private final int SIZE = 18;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(StorageCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    public StorageCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
        inventory = new ItemStackTransfer(SIZE) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (int slot = 0; slot < SIZE; slot++) {
            list.add(inventory.getStackInSlot(slot));
        }
        return list;
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 126, 87);

        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle())));

        for (int slot = 0; slot < SIZE; slot++) {
            group.addWidget(new SlotWidget(inventory, slot, 7 + (slot % 6) * 18, 21 + (slot / 6) * 18));
        }

        return group;
    }

    private String getUITitle() {
        return "cover.storage.title";
    }
}
