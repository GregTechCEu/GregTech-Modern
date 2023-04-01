package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.msic.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote BatteryBufferMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBufferMachine extends TieredEnergyMachine implements IControllable, IUIMachine {
    public static final long AMPS_PER_BATTERY = 2L;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BatteryBufferMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted @Getter @Setter
    private boolean isWorkingEnabled;
    @Getter
    private final int inventorySize;
    @Getter @Persisted
    protected final ItemStackTransfer batteryInventory;
    public BatteryBufferMachine(IMachineBlockEntity holder, int tier, int inventorySize, Object... args) {
        super(holder, tier, inventorySize);
        this.isWorkingEnabled = true;
        this.inventorySize = inventorySize;
        this.batteryInventory = createBatteryInventory(args);
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
        return new EnergyBatteryTrait((int)args[0]);
    }

    protected ItemStackTransfer createBatteryInventory(Object... args) {
        var itemTransfer = new ItemStackTransfer(this.inventorySize);
        itemTransfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return itemTransfer;
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        int colSize = rowSize;
        if (inventorySize == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var modular = new ModularUI(176, 18 + 18 * colSize + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * colSize + 12, true));

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                modular.widget(new SlotWidget(batteryInventory, index++, 88 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY)));
            }
        }

        return modular;
    }


    //////////////////////////////////////
    //******    Battery Logic     ******//
    //////////////////////////////////////

    private List<IElectricItem> getNonFullBatteries() {
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            var batteryStack = batteryInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(batteryStack);
            if (electricItem != null) {
                if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                    batteries.add(electricItem);
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

    private List<IElectricItem> getAllBatteries() {
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            var batteryStack = batteryInventory.getStackInSlot(i);
            var electricItem = GTCapabilityHelper.getElectricItem(batteryStack);
            if (electricItem != null) {
                batteries.add(electricItem);
            }
        }
        return batteries;
    }

    protected class EnergyBatteryTrait extends NotifiableEnergyContainer {

        protected EnergyBatteryTrait(int inventorySize) {
            super(BatteryBufferMachine.this, GTValues.V[tier] * inventorySize * 32L, GTValues.V[tier], inventorySize * AMPS_PER_BATTERY, GTValues.V[tier], inventorySize);
            this.setSideInputCondition(side -> side != getFrontFacing() && isWorkingEnabled());
            this.setSideOutputCondition(side -> side == getFrontFacing() && isWorkingEnabled());
        }

        @Override
        public void serverTick() {
            var outFacing = getFrontFacing();
            var energyContainer = GTCapabilityHelper.getEnergyContainer(getLevel(), getPos().relative(outFacing), outFacing.getOpposite());
            if (energyContainer == null) {
                return;
            }

            var voltage = getOutputVoltage();
            var batteries = getNonEmptyBatteries();
            if (batteries.size() > 0) {
                //Prioritize as many packets as available of energy created
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
                }

                if (changed) {
                    BatteryBufferMachine.this.markDirty();
                    checkOutputSubscription();
                }

                //Subtract energy created out of thin air from the buffer
                setEnergyStored(getInternalStorage() + internalAmps * voltage - energy);
            }
        }

        @Override
        public long acceptEnergyFromNetwork(@Nullable Direction side, long voltage, long amperage) {
            var latestTS = getMachine().getOffsetTimer();
            if (lastTS < latestTS) {
                amps = 0;
                lastTS = latestTS;
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

                //Prioritizes as many packets as available from the buffer
                long internalAmps = Math.min(leftAmps, Math.max(0, getInternalStorage() / voltage));

                usedAmps = Math.min(usedAmps, leftAmps - internalAmps);
                amps += usedAmps;

                long energy = (usedAmps + internalAmps) * voltage;
                long distributed = energy / batteries.size();

                boolean changed = false;
                for (var battery : batteries) {
                    var charged = battery.charge(Math.min(distributed, GTValues.V[battery.getTier()] * AMPS_PER_BATTERY), getTier(), true, false);
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                }

                if (changed) {
                    BatteryBufferMachine.this.markDirty();
                    checkOutputSubscription();
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
            for (IElectricItem battery : getAllBatteries()) {
                energyCapacity += battery.getMaxCharge();
            }
            return energyCapacity;
        }

        @Override
        public long getEnergyStored() {
            long energyStored = 0L;
            for (IElectricItem battery : getAllBatteries()) {
                energyStored += battery.getCharge();
            }
            return energyStored;
        }

        private long getInternalStorage() {
            return energyStored;
        }

    }
}
