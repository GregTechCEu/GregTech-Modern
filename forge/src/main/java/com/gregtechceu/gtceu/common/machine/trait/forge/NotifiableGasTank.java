package com.gregtechceu.gtceu.common.machine.trait.forge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.forge.GasRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.forge.core.mixins.mekanism.BasicChemicalTankAccessor;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("FunctionalExpressionCanBeFolded")
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NotifiableGasTank extends NotifiableRecipeHandlerTrait<GasStack> implements ICapabilityTrait, IGasHandler, IGasTank {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableGasTank.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter
    @Setter
    private long timeStamp;
    @Persisted
    public final ChemicalTankBuilder.BasicGasTank[] storages;
    @Setter
    protected boolean allowSameFluids; // Can different tanks be filled with the same fluid. It should be determined while creating tanks.
    @Nullable
    private Boolean isEmpty;

    public NotifiableGasTank(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = io;
        this.storages = new ChemicalTankBuilder.BasicGasTank[slots];
        this.capabilityIO = capabilityIO;
        for (int i = 0; i < this.storages.length; i++) {
            this.storages[i] = (ChemicalTankBuilder.BasicGasTank) ChemicalTankBuilder.GAS.createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, this::onContentsChanged);
        }
    }

    public NotifiableGasTank(MetaMachine machine, List<ChemicalTankBuilder.BasicGasTank> storages, IO io, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = io;
        this.storages = storages.toArray(ChemicalTankBuilder.BasicGasTank[]::new);
        this.capabilityIO = capabilityIO;
        for (ChemicalTankBuilder.BasicGasTank storage : this.storages) {
            ((BasicChemicalTankAccessor<?,?>)storage).setListener(this::onContentsChanged);
        }
    }

    public NotifiableGasTank(MetaMachine machine, int slots, long capacity, IO io) {
        this(machine, slots, capacity, io, io);
    }

    public NotifiableGasTank(MetaMachine machine, List<ChemicalTankBuilder.BasicGasTank> storages, IO io) {
        this(machine, storages, io, io);
    }

    public void onContentsChanged() {
        isEmpty = null;
        updateTimeStamp(machine.getLevel());
        notifyListeners();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    @Nullable
    public List<GasStack> handleRecipeInner(IO io, GTRecipe recipe, List<GasStack> left, @Nullable String slotName, boolean simulate) {
        if (io != this.handlerIO) return left;
        var capabilities = simulate ? Arrays.stream(storages).map(NotifiableGasTank::copyTank).toArray(IGasTank[]::new) : storages;
        for (IGasTank capability : capabilities) {
            Iterator<GasStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    GasStack gasStack = iterator.next();
                    if (gasStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    GasStack stored = capability.getStack();
                    if (!stored.isTypeEqual(gasStack)) {
                        continue;
                    }
                    GasStack drained = capability.extract(gasStack.getAmount(), Action.EXECUTE, AutomationType.INTERNAL);

                    gasStack.setAmount(gasStack.getAmount() - drained.getAmount());
                    if (gasStack.getAmount() <= 0) {
                        iterator.remove();
                    }
                }
            } else if (io == IO.OUT) {
                while (iterator.hasNext()) {
                    GasStack fluidStack = iterator.next();
                    if (fluidStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    GasStack filled = capability.insert(fluidStack.copy(), Action.EXECUTE, AutomationType.INTERNAL);
                    if (!fluidStack.isEmpty()) {
                        fluidStack.setAmount(fluidStack.getAmount() - filled.getAmount());
                    }
                    if (fluidStack.getAmount() <= 0) {
                        iterator.remove();
                    }
                }
            }
            if (left.isEmpty()) break;
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public RecipeCapability<GasStack> getCapability() {
        return GasRecipeCapability.CAP;
    }

    public int getTanks() {
        return storages.length;
    }

    @Override
    public GasStack getStack() {
        return storages[0].getStack();
    }

    @Override
    public void setStack(GasStack stack) {
        storages[0].setStack(stack);
    }

    @Override
    public void setStackUnchecked(GasStack stack) {

    }

    @Override
    public long getCapacity() {
        return this.getTankCapacity(0);
    }

    @Override
    public boolean isValid(GasStack stack) {
        return this.isValid(0, stack);
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (IGasTank storage : storages) {
                if (!storage.getStack().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public void exportToNearby(Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            NotifiableGasTank.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    public void importFromNearby(Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            NotifiableGasTank.importToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    //////////////////////////////////////
    //*******     Capability    ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public GasStack getChemicalInTank(int tank) {
        return storages[tank].getStack();
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull GasStack fluidStack) {
        storages[tank].setStack(fluidStack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return storages[tank].getCapacity();
    }

    @Override
    public boolean isValid(int tank, GasStack stack) {
        return storages[tank].isValid(stack);
    }

    @Override
    public GasStack insertChemical(int tank, GasStack stack, Action action) {
        return this.storages[tank].insert(stack, action, AutomationType.INTERNAL);
    }

    @Override
    public GasStack extractChemical(int tank, long amount, Action action) {
        return this.storages[tank].extract(amount, action, AutomationType.INTERNAL);
    }

    @Override
    public GasStack insert(GasStack stack, Action action, AutomationType automationType) {
        if (canCapInput()) {
            return fillInternal(stack, action, automationType);
        }
        return GasStack.EMPTY;
    }

    public GasStack fillInternal(GasStack resource, Action simulate, AutomationType automationType) {
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            IGasTank existingStorage = null;
            if (!allowSameFluids) {
                for (var storage : storages) {
                    if (!storage.getStack().isEmpty() && storage.getStack().isTypeEqual(resource)) {
                        existingStorage = storage;
                        break;
                    }
                }
            }
            if (existingStorage == null) {
                for (var storage : storages) {
                    var filled = storage.insert(copied.copy(), simulate, automationType);
                    if (filled.getAmount() > 0) {
                        copied.shrink(filled.getAmount());
                        if (!allowSameFluids) {
                            break;
                        }
                    }
                    if (copied.isEmpty()) break;
                }
            } else {
                copied.shrink(existingStorage.insert(copied.copy(), simulate, automationType).getAmount());
            }
            return new GasStack(resource, resource.getAmount() - copied.getAmount());
        }
        return GasStack.EMPTY;
    }

    @NotNull
    @Override
    public GasStack extract(long maxDrain, Action simulate, AutomationType automationType) {
        if (canCapInput()) {
            return drainInternal(maxDrain, simulate, automationType);
        }
        return GasStack.EMPTY;
    }

    public GasStack drainInternal(long maxDrain, Action simulate, AutomationType automationType) {
        if (maxDrain == 0) {
            return GasStack.EMPTY;
        }
        GasStack totalDrained = null;
        for (var storage : storages) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = storage.extract(maxDrain, simulate, automationType);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                GasStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                GasStack drain = storage.extract(copy.getAmount(), simulate, automationType);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? GasStack.EMPTY : totalDrained;
    }

    private static IGasTank copyTank(ChemicalTankBuilder.BasicGasTank toCopy) {
        //noinspection unchecked
        var tank = ChemicalTankBuilder.GAS.create(toCopy.getCapacity(), (gas) -> ((BasicChemicalTankAccessor<Gas, GasStack>)toCopy).getCanExtract().test(gas, AutomationType.MANUAL), (gas) -> ((BasicChemicalTankAccessor<Gas, GasStack>)toCopy).getCanInsert().test(gas, AutomationType.MANUAL), ((BasicChemicalTankAccessor<Gas, GasStack>)toCopy).getValidator(), ((BasicChemicalTankAccessor<Gas, GasStack>)toCopy).getAttributeValidator(), ((BasicChemicalTankAccessor<?, ?>) toCopy).getListener());
        tank.setStack(toCopy.getStack().copy());
        return tank;
    }
    
    @SuppressWarnings("SameParameterValue")
    private static void exportToTarget(IGasHandler source, int maxAmount, Predicate<GasStack> filter, Level level, BlockPos pos, @javax.annotation.Nullable Direction direction) {
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Optional<IGasHandler> cap = blockEntity.getCapability(Capabilities.GAS_HANDLER, direction).resolve();
                if (cap.isPresent()) {
                    IGasHandler target = cap.get();

                    for(int srcIndex = 0; srcIndex < source.getTanks(); ++srcIndex) {
                        GasStack currentFluid = source.getChemicalInTank(srcIndex);
                        if (!currentFluid.isEmpty() && filter.test(currentFluid)) {
                            GasStack toDrain = currentFluid.copy();
                            toDrain.setAmount(maxAmount);
                            GasStack filled = target.insertChemical(source.extractChemical(toDrain, Action.SIMULATE), Action.SIMULATE);
                            if (filled.getAmount() > 0) {
                                maxAmount -= filled.getAmount();
                                toDrain = currentFluid.copy();
                                toDrain.setAmount(filled.getAmount());
                                target.insertChemical(source.extractChemical(toDrain, Action.EXECUTE), Action.EXECUTE);
                            }

                            if (maxAmount <= 0) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void importToTarget(IGasHandler target, long maxAmount, Predicate<GasStack> filter, Level level, BlockPos pos, @javax.annotation.Nullable Direction direction) {
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Optional<IGasHandler> cap = blockEntity.getCapability(Capabilities.GAS_HANDLER, direction).resolve();
                if (cap.isPresent()) {
                    IGasHandler source = cap.get();

                    for(int srcIndex = 0; srcIndex < source.getTanks(); ++srcIndex) {
                        GasStack currentFluid = source.getChemicalInTank(srcIndex);
                        if (!currentFluid.isEmpty() && filter.test(currentFluid)) {
                            GasStack toDrain = currentFluid.copy();
                            toDrain.setAmount(maxAmount);
                            GasStack filled = target.insertChemical(source.extractChemical(toDrain, Action.SIMULATE), Action.SIMULATE);
                            if (filled.getAmount() > 0L) {
                                maxAmount = (maxAmount - filled.getAmount());
                                toDrain = currentFluid.copy();
                                toDrain.setAmount(filled.getAmount());
                                target.insertChemical(source.extractChemical(toDrain, Action.EXECUTE), Action.EXECUTE);
                            }

                            if (maxAmount <= 0) {
                                return;
                            }
                        }
                    }
                }
            }
        }

    }
}