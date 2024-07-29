package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import org.jetbrains.annotations.NotNull;

public class MEInputHatchPartMachine extends MEHatchPartMachine
                                     implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEInputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private ExportOnlyAEFluidList aeFluidTanks;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    @NotNull
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.aeFluidTanks = new ExportOnlyAEFluidList(this, slots, 0, IO.IN);
        return aeFluidTanks;
    }

    @Override
    @NotNull
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEFluidConfigWidget(3, 10, this.aeFluidTanks.tanks));

        return group;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tankSubs = this.aeFluidTanks.addChangedListener(this::updateTankSubscription);
    }

    @Override
    protected void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEFluidSlot aeTank : this.aeFluidTanks.tanks) {
                // Try to clear the wrong fluid
                GenericStack exceedFluid = aeTank.exceedStack();
                if (exceedFluid != null) {
                    long total = exceedFluid.amount();
                    long inserted = aeNetwork.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (inserted > 0) {
                        aeTank.drain(inserted, false);
                        continue;
                    } else {
                        aeTank.drain(total, false);
                    }
                }
                // Fill it
                GenericStack reqFluid = aeTank.requestStack();
                if (reqFluid != null) {
                    long extracted = aeNetwork.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (extracted > 0) {
                        aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                    }
                }
            }
            this.updateTankSubscription();
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
