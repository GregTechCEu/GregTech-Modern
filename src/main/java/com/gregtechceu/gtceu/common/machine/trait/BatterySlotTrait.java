package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ISubscription;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Machine trait that adds a battery charger slot to the side of the machine UI
 */
public class BatterySlotTrait extends MachineTrait implements IAttachConfiguratorsTrait {

    public static final MachineTraitType<BatterySlotTrait> TYPE = new MachineTraitType<>(BatterySlotTrait.class);
    @SaveField
    @Getter
    private final CustomItemStackHandler storage;

    @Nullable
    protected TickableSubscription batterySubs;
    @Nullable
    protected ISubscription energySubs;

    private final NotifiableEnergyContainer energyContainer;

    /**
     * Creates a battery charger slot trait
     * 
     * @param energyContainerToUse The energy container which the battery should draw energy from/push energy to.
     */
    public BatterySlotTrait(NotifiableEnergyContainer energyContainerToUse) {
        energyContainer = energyContainerToUse;
        storage = new CustomItemStackHandler(1);
        storage.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
    }

    @Override
    public MachineTraitType<BatterySlotTrait> getTraitType() {
        return TYPE;
    }

    @Override
    public void onMachineLoad() {
        if (!isRemote()) {
            updateBatterySubscription();
            energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
            storage.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onMachineUnload() {
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(storage, 0, true)) {
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        } else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(storage, 0, false)) {
            updateBatterySubscription();
        }
    }

    @Override
    public void onMachineDestroyed() {
        storage.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public void attachRightConfigurators(Flow flow, ModularPanel<?> panel, PanelSyncManager syncManager) {
        flow.child(GTMuiWidgets.createBatterySlot(this, syncManager));
    }
}
