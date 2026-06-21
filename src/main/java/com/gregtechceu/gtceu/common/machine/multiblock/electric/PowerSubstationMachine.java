package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.common.util.INBTSerializable;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BigIntegerSyncValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

public class PowerSubstationMachine extends WorkableMultiblockMachine
                                    implements IEnergyInfoProvider, IMuiMachine, IVoidable {

    // Structure Constants
    public static final int MAX_BATTERY_LAYERS = 18;
    public static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // Match Context Headers
    public static final String PMC_BATTERY_HEADER = "PSSBattery_";

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    private @Nullable IMaintenanceMachine maintenance;

    @SaveField
    private final PowerStationEnergyBank energyBank;

    private @Nullable EnergyContainerList inputHatches;
    private @Nullable EnergyContainerList outputHatches;
    private long passiveDrain;

    // Stats tracked for UI display
    private long netInLastSec;
    @Getter
    private long inputPerSec;
    private long netOutLastSec;
    @Getter
    private long outputPerSec;

    protected ConditionalSubscriptionHandler tickSubscription;

    public PowerSubstationMachine(BlockEntityCreationInfo info) {
        super(info);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isFormed);
        this.energyBank = attachTrait(new PowerStationEnergyBank(List.of()));
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                this.maintenance = maintenanceMachine;
            }
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;

                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();

                if (handlerList.getHandlerIO().support(IO.IN)) {
                    inputs.addAll(containers);
                } else if (handlerList.getHandlerIO().support(IO.OUT)) {
                    outputs.addAll(containers);
                }

                traitSubscriptions
                        .add(handlerList.subscribe(tickSubscription::updateSubscription, EURecipeCapability.CAP));
            }
        }
        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        List<IBatteryData> batteries = new ArrayList<>();
        for (Map.Entry<String, Object> battery : getMultiblockState().getMatchContext().entrySet()) {
            if (battery.getKey().startsWith(PMC_BATTERY_HEADER) &&
                    battery.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) {
                    batteries.add(wrapper.partType);
                }
            }
        }
        if (batteries.isEmpty()) {
            // only empty batteries found in the structure
            onStructureInvalid();
            return;
        }
        energyBank.rebuild(batteries);
        this.passiveDrain = this.energyBank.getPassiveDrainPerTick();
    }

    @Override
    public void onStructureInvalid() {
        // don't null out energyBank since it holds the stored energy, which
        // we need to hold on to across rebuilds to not void all energy if a
        // multiblock part or block other than the controller is broken.
        inputHatches = null;
        outputHatches = null;
        passiveDrain = 0;
        netInLastSec = 0;
        inputPerSec = 0;
        netOutLastSec = 0;
        outputPerSec = 0;
        super.onStructureInvalid();
    }

    protected void transferEnergyTick() {
        if (!getLevel().isClientSide) {
            if (getOffsetTimer() % 20 == 0) {
                // active here is just used for rendering
                getRecipeLogic()
                        .setStatus(energyBank.hasEnergy() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
                inputPerSec = netInLastSec;
                outputPerSec = netOutLastSec;
                netInLastSec = 0;
                netOutLastSec = 0;
            }

            if (isWorkingEnabled() && isFormed() && inputHatches != null && outputHatches != null) {
                // Bank from Energy Input Hatches
                long energyBanked = energyBank.fill(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBanked);
                netInLastSec += energyBanked;

                // Passive drain
                long energyPassiveDrained = energyBank.drain(getPassiveDrain());
                netOutLastSec += energyPassiveDrained;

                // Debank to Dynamo Hatches
                long energyDebanked = energyBank
                        .drain(outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());
                outputHatches.changeEnergy(energyDebanked);
                netOutLastSec += energyDebanked;
            }
        }
    }

    private static MutableComponent getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
    }

    public long getPassiveDrain() {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (maintenance == null) {
                for (IMultiPart part : getParts()) {
                    if (part instanceof IMaintenanceMachine maintenanceMachine) {
                        this.maintenance = maintenanceMachine;
                        break;
                    }
                }
            }
            int multiplier = 1 + maintenance.getNumMaintenanceProblems();
            double modifier = maintenance.getDurationMultiplier();
            return (long) (passiveDrain * multiplier * modifier);
        }
        return passiveDrain;
    }

    public String getStored() {
        return FormattingUtil.formatNumbers(energyBank.getStored());
    }

    public String getCapacity() {
        return FormattingUtil.formatNumbers(energyBank.getCapacity());
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return new EnergyInfo(energyBank.getCapacity(), energyBank.getStored());
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return true;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(new ParentWidget<>()
                .widthRel(0.95f)
                .heightRel(.65f)
                .margin(4, 0)
                .left(3).top(2)
                .horizontalCenter()
                .child(Flow.row()
                        .child(getMainTextPanel(syncManager, 186, 146))));
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>();
        listWidget
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .posRel(Alignment.CenterLeft)
                .left(3)
                .top(3);
        parentWidget.size(width, height)
                .background(GuiTextures.DISPLAY);
        // Machine generic sync handlers
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        BooleanSyncValue power = syncManager.getOrCreateSyncHandler("workingEnabled", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isWorkingEnabled, this.recipeLogic::setWorkingEnabled));
        BooleanSyncValue active = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isActive));
        BooleanSyncValue waiting = syncManager.getOrCreateSyncHandler("isWaiting", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isWaiting));

        // Energy bank specific sync handlers
        // These will not be called anywhere else, so we can create them directly instead of using
        // getOrCreateSyncHandler
        BooleanSyncValue energyBankExists = new BooleanSyncValue(() -> energyBank != null);
        syncManager.syncValue("energyBankExists", energyBankExists);

        BigIntegerSyncValue energyStored = new BigIntegerSyncValue(
                energyBank::getStored, $ -> {});
        syncManager.syncValue("energyStored", energyStored);

        BigIntegerSyncValue capacity = new BigIntegerSyncValue(
                energyBank::getCapacity, $ -> {});
        syncManager.syncValue("capacity", capacity);

        LongSyncValue passiveDrain = new LongSyncValue(this::getPassiveDrain);
        syncManager.syncValue("passiveDrain", passiveDrain);

        LongSyncValue inputPerSec = new LongSyncValue(() -> this.inputPerSec);
        syncManager.syncValue("inputPerSec", inputPerSec);

        LongSyncValue outputPerSec = new LongSyncValue(() -> this.outputPerSec);
        syncManager.syncValue("outputPerSec", outputPerSec);

        // Generic machine lines
        listWidget.child(Text.lang("gtceu.multiblock.work_paused")
                .asWidget()
                .setEnabledIf((widget) -> !power.getBoolValue()));
        listWidget.child(Text.lang("gtceu.multiblock.running")
                .asWidget()
                .setEnabledIf((widget) -> active.getBoolValue()));
        listWidget.child(Text.lang("gtceu.multiblock.idling")
                .asWidget()
                .setEnabledIf((widget) -> !active.getBoolValue() && power.getBoolValue()));
        listWidget.child(Text
                .of(Component.translatable("gtceu.multiblock.waiting")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)))
                .asWidget()
                .setEnabledIf((widget) -> waiting.getBoolValue()));

        // Energy bank specific lines

        var STYLE_GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
        var STYLE_DARK_RED = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
        var STYLE_GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
        var STYLE_RED = Style.EMPTY.withColor(ChatFormatting.RED);

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue()) return Component.empty();
            var storedComponent = Component.literal(FormattingUtil.formatNumbers(energyStored.getValue()));
            return Component.translatable("gtceu.multiblock.power_substation.stored",
                    storedComponent.setStyle(STYLE_GOLD));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue()));

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue()) return Component.empty();
            var capacityComponent = Component.literal(FormattingUtil.formatNumbers(capacity.getValue()));
            return Component.translatable("gtceu.multiblock.power_substation.capacity",
                    capacityComponent.setStyle(STYLE_GOLD));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue()));

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue()) return Component.empty();
            var passiveDrainComponent = Component.literal(FormattingUtil.formatNumbers(passiveDrain.getLongValue()));
            return Component.translatable("gtceu.multiblock.power_substation.passive_drain",
                    passiveDrainComponent.setStyle(STYLE_DARK_RED));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue()));

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue()) return Component.empty();
            var avgInComponent = Component.literal(FormattingUtil.formatNumbers(inputPerSec.getLongValue() / 20));
            return Component
                    .translatable("gtceu.multiblock.power_substation.average_in",
                            avgInComponent.setStyle(STYLE_GREEN))
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtceu.multiblock.power_substation.average_in_hover"))));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue()));
        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue()) return Component.empty();
            var avgOutComponent = Component
                    .literal(FormattingUtil.formatNumbers(Math.abs(outputPerSec.getLongValue() / 20)));
            return Component
                    .translatable("gtceu.multiblock.power_substation.average_out",
                            avgOutComponent.setStyle(STYLE_RED))
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtceu.multiblock.power_substation.average_out_hover"))));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue()));

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue() || inputPerSec.getLongValue() <= outputPerSec.getLongValue())
                return Component.empty();
            BigInteger timeToFillSeconds = capacity.getValue().subtract(energyStored.getValue())
                    .divide(BigInteger.valueOf(inputPerSec.getLongValue() - outputPerSec.getLongValue()));
            return Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                    getTimeToFillDrainText(timeToFillSeconds).setStyle(STYLE_GREEN));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue() &&
                        inputPerSec.getLongValue() > outputPerSec.getLongValue()));

        listWidget.child(Text.dynamic(() -> {
            if (!energyBankExists.getBoolValue() || inputPerSec.getLongValue() >= outputPerSec.getLongValue())
                return Component.empty();
            BigInteger timeToDrainSeconds = energyStored.getValue()
                    .divide(BigInteger.valueOf(outputPerSec.getLongValue() - inputPerSec.getLongValue()));
            return Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(timeToDrainSeconds).setStyle(STYLE_RED));
        })
                .asWidget()
                .setEnabledIf((widget) -> energyBankExists.getBoolValue() &&
                        inputPerSec.getLongValue() < outputPerSec.getLongValue()));
        parentWidget.child(listWidget);
        return parentWidget;
    }

    public static class PowerStationEnergyBank extends MachineTrait implements INBTSerializable<CompoundTag> {

        public static final MachineTraitType<PowerStationEnergyBank> TYPE = new MachineTraitType<>(
                PowerStationEnergyBank.class);

        @Override
        public MachineTraitType<PowerStationEnergyBank> getTraitType() {
            return TYPE;
        }

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";

        private long[] storage;
        private long[] maximums;
        @Getter
        private BigInteger capacity;
        private int index;

        public PowerStationEnergyBank(List<IBatteryData> batteries) {
            super();
            setupBatteries(batteries);
        }

        public void setupBatteries(List<IBatteryData> batteries) {
            storage = new long[batteries.size()];
            maximums = new long[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public void deserializeNBT(CompoundTag storageTag) {
            int size = storageTag.getInt(NBT_SIZE);
            storage = new long[size];
            maximums = new long[size];
            for (int i = 0; i < size; i++) {
                CompoundTag subtag = storageTag.getCompound(String.valueOf(i));
                if (subtag.contains(NBT_STORED)) {
                    storage[i] = subtag.getLong(NBT_STORED);
                }
                maximums[i] = subtag.getLong(NBT_MAX);
            }
            capacity = summarize(maximums);
        }

        public CompoundTag serializeNBT() {
            var compound = new CompoundTag();
            compound.putInt(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                CompoundTag subtag = new CompoundTag();
                if (storage[i] > 0) {
                    subtag.putLong(NBT_STORED, storage[i]);
                }
                subtag.putLong(NBT_MAX, maximums[i]);
                compound.put(String.valueOf(i), subtag);
            }
            return compound;
        }

        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public void rebuild(List<IBatteryData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            long[] oldStorage = storage.clone();
            setupBatteries(batteries);
            for (long stored : oldStorage) {
                fill(stored);
            }
        }

        /** @return Amount filled into storage */
        public long fill(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != storage.length - 1 && storage[index] == maximums[index]) {
                index++;
            }

            long maxFill = Math.min(maximums[index] - storage[index], amount);

            // storage is completely full
            if (maxFill == 0 && index == storage.length - 1) {
                return 0;
            }

            // fill this "battery" as much as possible
            storage[index] += maxFill;
            amount -= maxFill;

            // try to fill other "batteries" if necessary
            if (amount > 0 && index != storage.length - 1) {
                return maxFill + fill(amount);
            }

            // other fill not necessary, either because the storage is now completely full,
            // or we were able to consume all the energy in this "battery"
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != 0 && storage[index] == 0) {
                index--;
            }

            long maxDrain = Math.min(storage[index], amount);

            // storage is completely empty
            if (maxDrain == 0 && index == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[index] -= maxDrain;
            amount -= maxDrain;

            // try to drain other "batteries" if necessary
            if (amount > 0 && index != 0) {
                index--;
                return maxDrain + drain(amount);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
            return maxDrain;
        }

        public BigInteger getStored() {
            return summarize(storage);
        }

        public boolean hasEnergy() {
            for (long l : storage) {
                if (l > 0) return true;
            }
            return false;
        }

        private static BigInteger summarize(long[] values) {
            BigInteger retVal = BigInteger.ZERO;
            long currentSum = 0;
            for (long value : values) {
                if (currentSum != 0 && value > Long.MAX_VALUE - currentSum) {
                    // will overflow if added
                    retVal = retVal.add(BigInteger.valueOf(currentSum));
                    currentSum = 0;
                }
                currentSum += value;
            }
            if (currentSum != 0) {
                retVal = retVal.add(BigInteger.valueOf(currentSum));
            }
            return retVal;
        }

        @VisibleForTesting
        public long getPassiveDrainPerTick() {
            long[] maximumsExcl = new long[maximums.length];
            int index = 0;
            int numExcl = 0;
            for (long maximum : maximums) {
                if (maximum / PASSIVE_DRAIN_DIVISOR >= PASSIVE_DRAIN_MAX_PER_STORAGE) {
                    numExcl++;
                } else {
                    maximumsExcl[index++] = maximum;
                }
            }
            maximumsExcl = Arrays.copyOf(maximumsExcl, index);
            BigInteger capacityExcl = summarize(maximumsExcl);

            return capacityExcl.divide(BigInteger.valueOf(PASSIVE_DRAIN_DIVISOR))
                    .add(BigInteger.valueOf(PASSIVE_DRAIN_MAX_PER_STORAGE * numExcl))
                    .longValue();
        }
    }

    @Getter
    public static class BatteryMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public BatteryMatchWrapper(IBatteryData partType) {
            this.partType = partType;
        }

        public BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
