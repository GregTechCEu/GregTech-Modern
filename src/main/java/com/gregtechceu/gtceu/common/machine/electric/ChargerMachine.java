package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.data.GTDataComponents;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lucifer_ll
 * @date 2023/7/12
 * @implNote ChargerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChargerMachine extends TieredEnergyMachine implements IControllable, IFancyUIMachine, IMachineModifyDrops {
    public static final long AMPS_PER_ITEM = 4L;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    public enum State {
        IDLE,
        RUNNING,
        FINISHED
    }

    @Persisted
    @Getter
    @Setter
    private boolean isWorkingEnabled;
    @Getter
    private final int inventorySize;
    @Getter
    @Persisted(subPersisted = true)
    protected final CustomItemStackHandler chargerInventory;

    @Getter
    @DescSynced
    @RequireRerender
    private State state;

    public ChargerMachine(IMachineBlockEntity holder, int tier, int inventorySize, Object... args) {
        super(holder, tier, inventorySize);
        this.isWorkingEnabled = true;
        this.inventorySize = inventorySize;
        this.chargerInventory = createChargerInventory(args);
        this.state = State.IDLE;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        return new EnergyBatteryTrait((int) args[0]);
    }

    protected CustomItemStackHandler createChargerInventory(Object... args) {
        var itemTransfer = new CustomItemStackHandler(this.inventorySize);
        itemTransfer.setFilter(item -> item.get(GTDataComponents.ENERGY_CONTENT) != null || GTCapabilityHelper.getPlatformEnergyItem(item) != null);
        return itemTransfer;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, chargerInventory);
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
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
                (size.width - energyBar.getSize().width - 4 - template.getSize().width) / 2 + 2 + energyBar.getSize().width + 2,
                (size.height - template.getSize().height) / 2));
        group.addWidget(energyBar);
        group.addWidget(template);
        editableUI.setupUI(group, this);
        return group;
    }

    //////////////////////////////////////
    //******    Charger Logic     ******//
    //////////////////////////////////////

    private List<Pair<ItemStack, Object>> getNonFullElectricItem() {
        List<Pair<ItemStack, Object>> electricItems = new ArrayList<>();
        for (int i = 0; i < chargerInventory.getSlots(); i++) {
            var electricItemStack = chargerInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
            if (electricItem != null) {
                if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                    electricItems.add(Pair.of(electricItemStack, electricItem));
                }
            } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                var energyStorage = GTCapabilityHelper.getPlatformEnergyItem(electricItemStack);
                if (energyStorage != null) {
                    if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                        electricItems.add(Pair.of(electricItemStack, energyStorage));
                    }
                }
            }
        }
        return electricItems;
    }

    private void changeState(State newState) {
        if (state != newState) {
            state = newState;
        }
    }

    protected class EnergyBatteryTrait extends NotifiableEnergyContainer {

        protected EnergyBatteryTrait(int inventorySize) {
            super(ChargerMachine.this, GTValues.V[tier] * inventorySize * 32L, GTValues.V[tier], inventorySize * AMPS_PER_ITEM, 0L, 0L);
            this.setSideInputCondition(side -> isWorkingEnabled());
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
                changeState(State.IDLE);
                return 0;
            }

            var electricItems = getNonFullElectricItem();
            var maxAmps = electricItems.size() * AMPS_PER_ITEM - amps;
            var usedAmps = Math.min(maxAmps, amperage);
            if (maxAmps <= 0) {
                return 0;
            }

            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    doExplosion(GTUtil.getExplosionPower(voltage));
                    return usedAmps;
                }

                //Prioritizes as many packets as available from the buffer
                long internalAmps = Math.min(maxAmps, Math.max(0, getInternalStorage() / voltage));

                usedAmps = Math.min(usedAmps, maxAmps - internalAmps);
                amps += usedAmps;

                long energy = (usedAmps + internalAmps) * voltage;
                long distributed = energy / electricItems.size();

                boolean changed = false;
                var charged = 0L;
                for (var pair : electricItems) {
                    Object electricItem = pair.getSecond();
                    if (electricItem instanceof IElectricItem item) {
                        charged += item.charge(Math.min(distributed, GTValues.V[item.getTier()] * AMPS_PER_ITEM), getTier(), true, false);
                    } else if (electricItem instanceof IEnergyStorage energyStorage) {
                        energy += FeCompat.insertEu(energyStorage, Math.min(distributed, GTValues.V[getTier()] * AMPS_PER_ITEM));
                    }
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                }

                if (changed) {
                    ChargerMachine.this.markDirty();
                    changeState(State.RUNNING);
                }

                //Remove energy used and then transfer overflow energy into the internal buffer
                setEnergyStored(getInternalStorage() - internalAmps * voltage + energy);
                return usedAmps;
            }
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            long energyCapacity = 0L;
            for (int i = 0; i < chargerInventory.getSlots(); i++) {
                var electricItemStack = chargerInventory.getStackInSlot(i);
                var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
                if (electricItem != null) {
                    energyCapacity += electricItem.getMaxCharge();
                } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                    var energyStorage = GTCapabilityHelper.getPlatformEnergyItem(electricItemStack);
                    if (energyStorage != null) {
                        energyCapacity += FeCompat.toEu(energyStorage.getMaxEnergyStored(), FeCompat.ratio(false));
                    }
                }
            }

            if (energyCapacity == 0) {
                changeState(State.IDLE);
            }

            return energyCapacity;
        }

        @Override
        public long getEnergyStored() {
            long energyStored = 0L;
            for (int i = 0; i < chargerInventory.getSlots(); i++) {
                var electricItemStack = chargerInventory.getStackInSlot(i);
                var electricItem = GTCapabilityHelper.getElectricItem(electricItemStack);
                if (electricItem != null) {
                    energyStored += electricItem.getCharge();
                } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
                    var energyStorage = GTCapabilityHelper.getPlatformEnergyItem(electricItemStack);
                    if (energyStorage != null) {
                        energyStored += FeCompat.toEu(energyStorage.getEnergyStored(), FeCompat.ratio(false));
                    }
                }
            }

            var capacity = getEnergyCapacity();

            if (capacity != 0 && capacity == energyStored) {
                changeState(State.FINISHED);
            }

            return energyStored;
        }

        private long getInternalStorage() {
            return energyStored;
        }

    }
}
