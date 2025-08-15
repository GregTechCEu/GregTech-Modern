package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.energy.IEnergyStorage;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBufferMachine extends TieredEnergyMachine
                                  implements IControllable, IFancyUIMachine, IMachineLife, IMonitorComponent {

    public static final long AMPS_PER_BATTERY = 2L;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BatteryBufferMachine.class,
            TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    private boolean isWorkingEnabled;
    @Getter
    private final int inventorySize;
    @Getter
    @Persisted
    protected final CustomItemStackHandler batteryInventory;

    public BatteryBufferMachine(IMachineBlockEntity holder, int tier, int inventorySize, Object... args) {
        super(holder, tier, inventorySize);
        this.isWorkingEnabled = true;
        this.inventorySize = inventorySize;
        this.batteryInventory = createBatteryInventory(args);
        this.batteryInventory.setOnContentsChanged(energyContainer::checkOutputSubscription);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        return new EnergyBatteryTrait((int) args[0]);
    }

    protected CustomItemStackHandler createBatteryInventory(Object... ignoredArgs) {
        var handler = new CustomItemStackHandler(this.inventorySize) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(inventorySize);
        int colSize = rowSize;
        if (inventorySize == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var template = new WidgetGroup(0, 0, 18 * rowSize + 8, 18 * colSize + 8);
        template.setBackground(GuiTextures.BACKGROUND_INVERSE);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                template.addWidget(new SlotWidget(batteryInventory, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY)));
            }
        }

        var editableUI = createEnergyBar();
        var energyBar = editableUI.createDefault();

        var group = new WidgetGroup(0, 0,
                Math.max(energyBar.getSize().width + template.getSize().width + 4 + 8, 172),
                Math.max(template.getSize().height + 8, energyBar.getSize().height + 8));
        var size = group.getSize();
        energyBar.setSelfPosition(new Position(3, (size.height - energyBar.getSize().height) / 2));
        template.setSelfPosition(new Position(
                (size.width - energyBar.getSize().width - 4 - template.getSize().width) / 2 + 2 +
                        energyBar.getSize().width + 2,
                (size.height - template.getSize().height) / 2));
        group.addWidget(energyBar);
        group.addWidget(template);
        editableUI.setupUI(group, this);
        return group;
    }

    //////////////////////////////////////
    // ****** Battery Logic ******//
    //////////////////////////////////////

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        energyContainer.checkOutputSubscription();
    }

    private List<Object> getNonFullBatteries() {
        List<Object> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            var batteryStack = batteryInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(batteryStack);
            if (electricItem != null) {
                if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                    batteries.add(electricItem);
                }
            } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                IEnergyStorage energyStorage = GTCapabilityHelper.getForgeEnergyItem(batteryStack);
                if (energyStorage != null) {
                    if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                        batteries.add(energyStorage);
                    }
                }
            }
        }
        return batteries;
    }

    private List<IElectricItem> getNonEmptyBatteries() {
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            var batteryStack = batteryInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(batteryStack);
            if (electricItem != null) {
                if (electricItem.canProvideChargeExternally() && electricItem.getCharge() > 0) {
                    batteries.add(electricItem);
                }
            }
        }
        return batteries;
    }

    private List<Object> getAllBatteries() {
        List<Object> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            var batteryStack = batteryInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(batteryStack);
            if (electricItem != null) {
                batteries.add(electricItem);
            } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                IEnergyStorage energyStorage = GTCapabilityHelper.getForgeEnergyItem(batteryStack);
                if (energyStorage != null) {
                    batteries.add(energyStorage);
                }
            }
        }
        return batteries;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(batteryInventory);
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return GuiTextures.BUTTON_CHECK; // temporary
    }

    protected class EnergyBatteryTrait extends NotifiableEnergyContainer {

        protected EnergyBatteryTrait(int inventorySize) {
            super(BatteryBufferMachine.this, GTValues.V[tier] * inventorySize * 32L, GTValues.V[tier],
                    inventorySize * AMPS_PER_BATTERY, GTValues.V[tier], inventorySize);
            this.setSideInputCondition(side -> side != getFrontFacing() && isWorkingEnabled());
            this.setSideOutputCondition(side -> side == getFrontFacing() && isWorkingEnabled());
        }

        @Override
        public void checkOutputSubscription() {
            if (isWorkingEnabled()) {
                super.checkOutputSubscription();
            } else if (outputSubs != null) {
                outputSubs.unsubscribe();
                outputSubs = null;
            }
        }

        @Override
        public void serverTick() {
            var outFacing = getFrontFacing();
            var energyContainer = GTCapabilityHelper.getEnergyContainer(getLevel(), getPos().relative(outFacing),
                    outFacing.getOpposite());
            if (energyContainer == null) {
                return;
            }

            var voltage = getOutputVoltage();
            var batteries = getNonEmptyBatteries();
            if (!batteries.isEmpty()) {
                // Prioritize as many packets as available of energy created
                long internalAmps = Math.abs(Math.min(0, getInternalStorage() / voltage));
                long genAmps = Math.max(0, batteries.size() - internalAmps);
                long outAmps = 0L;

                if (genAmps > 0) {
                    outAmps = energyContainer.acceptEnergyFromNetwork(outFacing.getOpposite(), voltage, genAmps);
                    if (outAmps == 0 && internalAmps == 0)
                        return;
                }

                long energy = (outAmps + internalAmps) * voltage;
                long distributed = energy / batteries.size();

                boolean changed = false;
                for (IElectricItem electricItem : batteries) {
                    var charged = electricItem.discharge(distributed, getTier(), false, true, false);
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                    energyOutputPerSec += charged;
                }

                if (changed) {
                    BatteryBufferMachine.this.markDirty();
                    checkOutputSubscription();
                }

                // Subtract energy created out of thin air from the buffer
                setEnergyStored(getInternalStorage() + internalAmps * voltage - energy);
            }
        }

        @Override
        public long acceptEnergyFromNetwork(@Nullable Direction side, long voltage, long amperage) {
            var latestTimeStamp = getMachine().getOffsetTimer();
            if (lastTimeStamp < latestTimeStamp) {
                amps = 0;
                lastTimeStamp = latestTimeStamp;
            }
            if (amperage <= 0 || voltage <= 0)
                return 0;

            var batteries = getNonFullBatteries();
            var leftAmps = batteries.size() * AMPS_PER_BATTERY - amps;
            var usedAmps = Math.min(leftAmps, amperage);
            if (leftAmps <= 0)
                return 0;

            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    doExplosion(GTUtil.getExplosionPower(voltage));
                    return usedAmps;
                }

                // Prioritizes as many packets as available from the buffer
                long internalAmps = Math.min(leftAmps, Math.max(0, getInternalStorage() / voltage));

                usedAmps = Math.min(usedAmps, leftAmps - internalAmps);
                amps += usedAmps;

                long energy = (usedAmps + internalAmps) * voltage;
                long distributed = energy / batteries.size();

                boolean changed = false;
                for (Object item : batteries) {
                    long charged = 0;
                    if (item instanceof IElectricItem electricItem) {
                        charged = electricItem.charge(
                                Math.min(distributed, GTValues.V[electricItem.getTier()] * AMPS_PER_BATTERY), getTier(),
                                true, false);
                    } else if (item instanceof IEnergyStorage energyStorage) {
                        charged = FeCompat.insertEu(energyStorage,
                                Math.min(distributed, GTValues.V[getTier()] * AMPS_PER_BATTERY), false);
                    }
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                    energyInputPerSec += charged;
                }

                if (changed) {
                    BatteryBufferMachine.this.markDirty();
                    checkOutputSubscription();
                }

                // Remove energy used and then transfer overflow energy into the internal buffer
                setEnergyStored(getInternalStorage() - internalAmps * voltage + energy);
                return usedAmps;
            }
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            long energyCapacity = 0L;
            for (Object battery : getAllBatteries()) {
                if (battery instanceof IElectricItem electricItem) {
                    energyCapacity += electricItem.getMaxCharge();
                } else if (battery instanceof IEnergyStorage energyStorage) {
                    energyCapacity += FeCompat.toEu(energyStorage.getMaxEnergyStored(), FeCompat.ratio(false));
                }
            }
            return energyCapacity;
        }

        @Override
        public long getEnergyStored() {
            long energyStored = 0L;
            for (Object battery : getAllBatteries()) {
                if (battery instanceof IElectricItem electricItem) {
                    energyStored += electricItem.getCharge();
                } else if (battery instanceof IEnergyStorage energyStorage) {
                    energyStored += FeCompat.toEu(energyStorage.getEnergyStored(), FeCompat.ratio(false));
                }
            }
            return energyStored;
        }

        private long getInternalStorage() {
            return energyStored;
        }
    }
}
