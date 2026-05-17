package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.energy.IEnergyStorage;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BatteryBufferMachine extends TieredEnergyMachine
                                  implements IControllable, IMuiMachine, IMonitorComponent {

    public static final long AMPS_PER_BATTERY_NORMAL = 2L;
    public static final long AMPS_PER_BATTERY_CHARGER = 4L;

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

    public static final EnumProperty<State> STATE_PROPERTY = GTMachineModelProperties.CHARGER_STATE;

    @SaveField
    @Getter
    private boolean isWorkingEnabled;
    @Getter
    private final int inventorySize;
    @Getter
    @SaveField
    protected final CustomItemStackHandler batteryInventory;

    @Getter
    @SyncToClient
    @RerenderOnChanged
    private State state;

    public BatteryBufferMachine(BlockEntityCreationInfo info, int tier, int inventorySize, long inputAmpsPerItem,
                                long outputAmps) {
        super(info, tier, new EnergyBatteryTrait(tier, inventorySize, inputAmpsPerItem, outputAmps));
        this.isWorkingEnabled = true;
        this.inventorySize = inventorySize;

        this.batteryInventory = new CustomItemStackHandler(this.inventorySize) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        this.batteryInventory.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));

        this.batteryInventory.setOnContentsChanged(energyContainer::checkOutputSubscription);
        this.state = State.IDLE;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("chargerInventory"))
            tag.put("batteryInventory", Objects.requireNonNull(tag.get("chargerInventory")));
        super.load(tag);
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    private void changeState(State newState) {
        if (state != newState) {
            state = newState;
            syncDataHolder.markClientSyncFieldDirty("state");
            if (getRenderState().hasProperty(GTMachineModelProperties.CHARGER_STATE)) {
                setRenderState(getRenderState().setValue(GTMachineModelProperties.CHARGER_STATE, newState));
            }
        }
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    // TODO add EIO widget
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        String[] matrix;
        if (inventorySize == 8) matrix = new String[] { "BBBB", "BBBB" };
        else matrix = GTMuiMachineUtil.createSquareMatrix(inventorySize, 'B');

        DoubleSyncValue energyPercentage = syncManager.getOrCreateSyncHandler("energyPercentage", DoubleSyncValue.class,
                () -> new DoubleSyncValue(this::getEnergyPercentage));

        var flow = Flow.row().width(MachineUIPanel.DEFAULT_CONTENT_WIDTH).height(90);

        flow.child(new ProgressWidget()
                .texture(GTGuiTextures.PROGRESS_BAR_BOILER_EMPTY_STEEL,
                        GTGuiTextures.PROGRESS_BAR_BOILER_HEAT, ProgressDrawable.Direction.UP)
                .value(energyPercentage)
                .marginLeft(5)
                .size(18, 60)
                .addTooltipLine(Text.dynamic(() -> Component.literal(
                        "%s/%s EU".formatted(
                                GTStringUtils.formatInt(energyContainer.getEnergyStored()),
                                GTStringUtils.formatInt(energyContainer.getEnergyCapacity()))))))
                .child(GTMuiMachineUtil.createSlotGroupFromInventory(
                        batteryInventory, "batteries",
                        inventorySize, 'B',
                        slot -> slot.background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY),
                        syncManager,
                        matrix)
                        .center());

        mainWidget.child(flow);
    }

    private double getEnergyPercentage() {
        return (double) this.energyContainer.getEnergyStored() / this.energyContainer.getEnergyCapacity();
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
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        batteryInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public IDrawable getIcon() {
        return GTGuiTextures.BATTERY_OVERLAY;
    }

    protected static class EnergyBatteryTrait extends NotifiableEnergyContainer {

        private final int tier;
        private final long inputAmpsPerItem;

        protected EnergyBatteryTrait(int tier, int inventorySize, long inputAmpsPerItem, long outputAmps) {
            super(GTValues.V[tier] * inventorySize * 32L, GTValues.V[tier],
                    inventorySize * inputAmpsPerItem, outputAmps == 0 ? 0 : GTValues.V[tier], outputAmps);
            this.tier = tier;
            this.inputAmpsPerItem = inputAmpsPerItem;
            this.setSideInputCondition(
                    side -> side != getMachine().getFrontFacing() && getMachine().isWorkingEnabled());
            this.setSideOutputCondition(
                    side -> side == getMachine().getFrontFacing() && getMachine().isWorkingEnabled());
        }

        @Override
        public BatteryBufferMachine getMachine() {
            return (BatteryBufferMachine) super.getMachine();
        }

        @Override
        protected List<Class<?>> validMachineClasses() {
            return List.of(BatteryBufferMachine.class);
        }

        @Override
        public void checkOutputSubscription() {
            if (getMachine().isWorkingEnabled()) {
                super.checkOutputSubscription();
            } else if (outputSubs != null) {
                outputSubs.unsubscribe();
                outputSubs = null;
            }
        }

        @Override
        public void serverTick() {
            var outFacing = getMachine().getFrontFacing();
            var energyContainer = GTCapabilityHelper.getEnergyContainer(getLevel(),
                    getBlockPos().relative(outFacing),
                    outFacing.getOpposite());
            if (energyContainer == null) {
                return;
            }

            var voltage = getOutputVoltage();
            var batteries = getMachine().getNonEmptyBatteries();
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
                    var charged = electricItem.discharge(distributed, tier, false, true, false);
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                    energyOutputPerSec += charged;
                }

                if (changed) {
                    getMachine().markAsChanged();
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
            if (amperage <= 0 || voltage <= 0) {
                getMachine().changeState(BatteryBufferMachine.State.IDLE);
                return 0;
            }

            var batteries = getMachine().getNonFullBatteries();
            var leftAmps = batteries.size() * inputAmpsPerItem - amps;
            var usedAmps = Math.min(leftAmps, amperage);
            if (leftAmps <= 0)
                return 0;

            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    GTUtil.doExplosion(getLevel(), getBlockPos(), GTUtil.getExplosionPower(voltage));
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
                                Math.min(distributed, GTValues.V[electricItem.getTier()] * inputAmpsPerItem), tier,
                                true, false);
                    } else if (item instanceof IEnergyStorage energyStorage) {
                        charged = FeCompat.insertEu(energyStorage,
                                Math.min(distributed, GTValues.V[tier] * inputAmpsPerItem), false);
                    }
                    if (charged > 0) {
                        changed = true;
                    }
                    energy -= charged;
                    energyInputPerSec += charged;
                }

                if (changed) {
                    getMachine().markAsChanged();
                    getMachine().changeState(BatteryBufferMachine.State.RUNNING);
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
            for (Object battery : getMachine().getAllBatteries()) {
                if (battery instanceof IElectricItem electricItem) {
                    energyCapacity += electricItem.getMaxCharge();
                } else if (battery instanceof IEnergyStorage energyStorage) {
                    energyCapacity += FeCompat.toEu(energyStorage.getMaxEnergyStored(), FeCompat.ratio(false));
                }
            }

            if (energyCapacity == 0) {
                getMachine().changeState(BatteryBufferMachine.State.IDLE);
            }

            return energyCapacity;
        }

        @Override
        public long getEnergyStored() {
            long energyStored = 0L;
            for (Object battery : getMachine().getAllBatteries()) {
                if (battery instanceof IElectricItem electricItem) {
                    energyStored += electricItem.getCharge();
                } else if (battery instanceof IEnergyStorage energyStorage) {
                    energyStored += FeCompat.toEu(energyStorage.getEnergyStored(), FeCompat.ratio(false));
                }
            }

            var capacity = getEnergyCapacity();

            if (capacity != 0 && capacity == energyStored) {
                getMachine().changeState(BatteryBufferMachine.State.FINISHED);
            }

            return energyStored;
        }

        private long getInternalStorage() {
            return energyStored;
        }
    }
}
