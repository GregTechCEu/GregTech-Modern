package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

public class MEInputBusPartMachine extends MEBusPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusPartMachine.class,
            MEBusPartMachine.MANAGED_FIELD_HOLDER);
    private final static int CONFIG_SIZE = 16;

    private ExportOnlyAEItemList aeItemHandler;

    public MEInputBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.inventory) {
                // Try to clear the wrong item
                GenericStack exceedItem = aeSlot.exceedStack();
                if (exceedItem != null) {
                    long total = exceedItem.amount();
                    long inserted = aeNetwork.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (inserted > 0) {
                        aeSlot.extractItem(0, (int) inserted, false);
                        continue;
                    } else {
                        aeSlot.extractItem(0, (int) total, false);
                    }
                }
                // Fill it
                GenericStack reqItem = aeSlot.requestStack();
                if (reqItem != null) {
                    long extracted = aeNetwork.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (extracted != 0) {
                        aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                    }
                }
            }
            this.updateInventorySubscription();
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEItemConfigWidget(3, 10, this.aeItemHandler.inventory));

        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
