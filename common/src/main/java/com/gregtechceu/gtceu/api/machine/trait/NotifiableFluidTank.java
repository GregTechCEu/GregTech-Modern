package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote NotifiableFluidTank
 */
public class NotifiableFluidTank extends NotifiableRecipeHandlerTrait<FluidIngredient> implements ICapabilityTrait, IFluidTransfer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableFluidTank.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter
    @Setter
    private long timeStamp;
    @Persisted
    public final FluidStorage[] storages;
    @Setter
    protected boolean allowSameFluids; // Can different tanks be filled with the same fluid. It should be determined while creating tanks.
    private Boolean isEmpty;

    public NotifiableFluidTank(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = io;
        this.storages = new FluidStorage[slots];
        this.capabilityIO = capabilityIO;
        for (int i = 0; i < this.storages.length; i++) {
            this.storages[i] = new FluidStorage(capacity);
            this.storages[i].setOnContentsChanged(this::onContentsChanged);
        }
    }

    public NotifiableFluidTank(MetaMachine machine, List<FluidStorage> storages, IO io, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = io;
        this.storages = storages.toArray(FluidStorage[]::new);
        this.capabilityIO = capabilityIO;
        for (FluidStorage storage : this.storages) {
            storage.setOnContentsChanged(this::onContentsChanged);
        }
    }

    public NotifiableFluidTank(MetaMachine machine, int slots, long capacity, IO io) {
        this(machine, slots, capacity, io, io);
    }

    public NotifiableFluidTank(MetaMachine machine, List<FluidStorage> storages, IO io) {
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
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
        if (io != this.handlerIO) return left;
        var capabilities = simulate ? Arrays.stream(storages).map(FluidStorage::copy).toArray(FluidStorage[]::new) : storages;
        for (FluidStorage capability : capabilities) {
            Iterator<FluidIngredient> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    FluidIngredient fluidStack = iterator.next();
                    if (fluidStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    boolean found = false;
                    FluidStack foundStack = null;
                    for (int i = 0; i < capability.getTanks(); i++) {
                        FluidStack stored = capability.getFluidInTank(i);
                        if (!fluidStack.test(stored)) {
                            continue;
                        }
                        found = true;
                        foundStack = stored;
                    }
                    if (!found) continue;
                    FluidStack drained = capability.drain(foundStack.copy(fluidStack.getAmount()), false);

                    fluidStack.setAmount(fluidStack.getAmount() - drained.getAmount());
                    if (fluidStack.getAmount() <= 0) {
                        iterator.remove();
                    }
                }
            } else if (io == IO.OUT) {
                while (iterator.hasNext()) {
                    FluidIngredient fluidStack = iterator.next();
                    if (fluidStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    var fluids = fluidStack.getStacks();
                    if (fluids.length == 0) {
                        iterator.remove();
                        continue;
                    }
                    FluidStack output = fluids[0];
                    long filled = capability.fill(output.copy(), false);
                    if (!fluidStack.isEmpty()) {
                        fluidStack.setAmount(fluidStack.getAmount() - filled);
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

    public NotifiableFluidTank setFilter(Predicate<FluidStack> filter) {
        for (FluidStorage storage : storages) {
            storage.setValidator(filter);
        }
        return this;
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    public int getTanks() {
        return storages.length;
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (FluidStorage storage : storages) {
                if (!storage.getFluid().isEmpty()) {
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
            FluidTransferHelper.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    public void importFromNearby(Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            FluidTransferHelper.importToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    //////////////////////////////////////
    //*******     Capability    ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return storages[tank].getFluid();
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        storages[tank].setFluid(fluidStack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return storages[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return storages[tank].isFluidValid(stack);
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (tank >= 0 && tank < storages.length && canCapInput()) {
            return storages[tank].fill(resource, simulate, notifyChanges);
        }
        return 0;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate) {
        if (canCapInput()) {
            return fillInternal(resource, simulate);
        }
        return 0;
    }

    public long fillInternal(FluidStack resource, boolean simulate) {
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            FluidStorage existingStorage = null;
            if (!allowSameFluids) {
                for (var storage : storages) {
                    if (!storage.getFluid().isEmpty() && storage.getFluid().isFluidEqual(resource)) {
                        existingStorage = storage;
                        break;
                    }
                }
            }
            if (existingStorage == null) {
                for (var storage : storages) {
                    var filled = storage.fill(copied.copy(), simulate);
                    if (filled > 0) {
                        copied.shrink(filled);
                        if (!allowSameFluids) {
                            break;
                        }
                    }
                    if (copied.isEmpty()) break;
                }
            } else {
                copied.shrink(existingStorage.fill(copied.copy(), simulate));
            }
            return resource.getAmount() - copied.getAmount();
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (tank >= 0 && tank < storages.length && canCapOutput()) {
            return storages[tank].drain(resource, simulate, notifyChanges);
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, boolean simulate) {
        if (canCapOutput()) {
            return drainInternal(resource, simulate);
        }
        return FluidStack.empty();
    }

    public FluidStack drainInternal(FluidStack resource, boolean simulate) {
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            for (var transfer : storages) {
                var candidate = copied.copy();
                copied.shrink(transfer.drain(candidate, simulate).getAmount());
                if (copied.isEmpty()) break;
            }
            copied.setAmount(resource.getAmount() - copied.getAmount());
            return copied;
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean simulate) {
        if (canCapInput()) {
            return drainInternal(maxDrain, simulate);
        }
        return FluidStack.empty();
    }

    public FluidStack drainInternal(long maxDrain, boolean simulate) {
        if (maxDrain == 0) {
            return FluidStack.empty();
        }
        FluidStack totalDrained = null;
        for (var storage : storages) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = storage.drain(maxDrain, simulate);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = storage.drain(copy, simulate);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.empty() : totalDrained;
    }

    @Override
    public boolean supportsFill(int i) {
        return canCapInput();
    }

    @Override
    public boolean supportsDrain(int i) {
        return canCapOutput();
    }


    @NotNull
    @Override
    public Object createSnapshot() {
        return Arrays.stream(storages).map(IFluidTransfer::createSnapshot).toArray(Object[]::new);
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Object[] array && array.length == storages.length) {
            for (int i = 0; i < array.length; i++) {
                storages[i].restoreFromSnapshot(array[i]);
            }
        }
    }

}