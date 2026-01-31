package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.energy.IEnergyStorage;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChargerMachine extends TieredEnergyMachine implements IControllable, IFancyUIMachine, IMachineLife {

    public static final long AMPS_PER_ITEM = 4L;

    public enum State implements StringRepresentable {

        IDLE("idle"),
        RUNNING("running"),
        FINISHED("finished");

        @Getter
        private final String serializedName;

        State(String name) {
            this.serializedName = name;
        }
    }

    public static final EnumProperty<ChargerMachine.State> STATE_PROPERTY = GTMachineModelProperties.CHARGER_STATE;

    @SaveField
    @Getter
    @Setter
    private boolean isWorkingEnabled;
    @Getter
    private final int inventorySize;
    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;

    @Getter
    @SyncToClient
    @RerenderOnChanged
    private State state;

    public ChargerMachine(BlockEntityCreationInfo info, int tier, int inventorySize) {
        super(info, tier,
                (TieredEnergyMachine machine) -> new EnergyBatteryTrait((ChargerMachine) machine, inventorySize));
        this.isWorkingEnabled = true;
        this.inventorySize = inventorySize;
        this.chargerInventory = createChargerInventory();
        this.state = State.IDLE;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected CustomItemStackHandler createChargerInventory() {
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

    @Override
    public void onMachineRemoved() {
        clearInventory(chargerInventory);
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
                template.addWidget(new SlotWidget(chargerInventory, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)));
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
    // ****** Charger Logic ******//
    //////////////////////////////////////

    private List<Object> getNonFullElectricItem() {
        List<Object> electricItems = new ArrayList<>();
        for (int i = 0; i < chargerInventory.getSlots(); i++) {
            var electricItemStack = chargerInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
            if (electricItem != null) {
                if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                    electricItems.add(electricItem);
                }
            } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                var energyStorage = GTCapabilityHelper.getForgeEnergyItem(electricItemStack);
                if (energyStorage != null) {
                    if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                        electricItems.add(energyStorage);
                    }
                }
            }
        }
        return electricItems;
    }

    private void changeState(State newState) {
        if (state != newState) {
            state = newState;
            syncDataHolder.markClientSyncFieldDirty("state");
            setRenderState(getRenderState().setValue(GTMachineModelProperties.CHARGER_STATE, newState));
        }
    }

    protected static class EnergyBatteryTrait extends NotifiableEnergyContainer {

        private ChargerMachine machine;

        protected EnergyBatteryTrait(ChargerMachine machine, int inventorySize) {
            super(machine, GTValues.V[machine.tier] * inventorySize * 32L, GTValues.V[machine.tier],
                    inventorySize * AMPS_PER_ITEM, 0L, 0L);
            this.setSideInputCondition(side -> machine.isWorkingEnabled());
            this.setSideOutputCondition(side -> false);
        }

        @Override
        public long acceptEnergyFromNetwork(@Nullable Direction side, long voltage, long amperage) {
            var latestTimeStamp = getMachine().getOffsetTimer();
            if (lastTimeStamp < latestTimeStamp) {
                amps = 0;
                lastTimeStamp = latestTimeStamp;
            }
            if (amperage <= 0 || voltage <= 0) {
                machine.changeState(State.IDLE);
                return 0;
            }

            var electricItems = machine.getNonFullElectricItem();
            var maxAmps = electricItems.size() * AMPS_PER_ITEM - amps;
            var usedAmps = Math.min(maxAmps, amperage);
            if (maxAmps <= 0) {
                return 0;
            }

            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    machine.doExplosion(GTUtil.getExplosionPower(voltage));
                    return usedAmps;
                }

                // Prioritizes as many packets as available from the buffer
                long internalAmps = Math.min(maxAmps, Math.max(0, getInternalStorage() / voltage));

                usedAmps = Math.min(usedAmps, maxAmps - internalAmps);
                amps += usedAmps;

                long energy = (usedAmps + internalAmps) * voltage;
                long distributed = energy / electricItems.size();

                boolean changed = false;
                for (var electricItem : electricItems) {
                    long charged = 0;
                    if (electricItem instanceof IElectricItem item) {
                        charged = item.charge(Math.min(distributed, GTValues.V[item.getTier()] * AMPS_PER_ITEM),
                                machine.tier, true, false);
                    } else if (electricItem instanceof IEnergyStorage energyStorage) {
                        charged = FeCompat.insertEu(energyStorage,
                                Math.min(distributed, GTValues.V[machine.tier] * AMPS_PER_ITEM), false);
                    }
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                    energyInputPerSec += charged;
                }

                if (changed) {
                    machine.markAsDirty();
                    machine.changeState(State.RUNNING);
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
            for (int i = 0; i < machine.chargerInventory.getSlots(); i++) {
                var electricItemStack = machine.chargerInventory.getStackInSlot(i);
                var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
                if (electricItem != null) {
                    energyCapacity += electricItem.getMaxCharge();
                } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                    var energyStorage = GTCapabilityHelper.getForgeEnergyItem(electricItemStack);
                    if (energyStorage != null) {
                        energyCapacity += FeCompat.toEu(energyStorage.getMaxEnergyStored(),
                                FeCompat.ratio(false));
                    }
                }
            }

            if (energyCapacity == 0) {
                machine.changeState(State.IDLE);
            }

            return energyCapacity;
        }

        @Override
        public long getEnergyStored() {
            long energyStored = 0L;
            for (int i = 0; i < machine.chargerInventory.getSlots(); i++) {
                var electricItemStack = machine.chargerInventory.getStackInSlot(i);
                var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
                if (electricItem != null) {
                    energyStored += electricItem.getCharge();
                } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                    var energyStorage = GTCapabilityHelper.getForgeEnergyItem(electricItemStack);
                    if (energyStorage != null) {
                        energyStored += FeCompat.toEu(energyStorage.getEnergyStored(),
                                FeCompat.ratio(false));
                    }
                }
            }

            var capacity = getEnergyCapacity();

            if (capacity != 0 && capacity == energyStored) {
                machine.changeState(State.FINISHED);
            }

            return energyStored;
        }

        private long getInternalStorage() {
            return energyStored;
        }
    }
}
