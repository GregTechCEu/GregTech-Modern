package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtlib.misc.FluidStorage;
import com.gregtechceu.gtlib.side.fluid.FluidStack;
import com.gregtechceu.gtlib.side.fluid.FluidTransferHelper;
import com.gregtechceu.gtlib.side.fluid.IFluidTransfer;
import com.gregtechceu.gtlib.syncdata.annotation.Persisted;
import com.gregtechceu.gtlib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote NotifiableFluidTank
 */
public class NotifiableFluidTank extends NotifiableRecipeHandlerTrait<FluidStack> implements ICapabilityTrait, IFluidTransfer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableFluidTank.class);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter
    @Setter
    private long timeStamp;
    @Persisted
    public final FluidStorage[] storages;
    private Boolean isEmpty;

    public NotifiableFluidTank(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = io;
        this.storages = new FluidStorage[slots];
        this.capabilityIO = capabilityIO;
        for (int i = 0; i < this.storages.length; i++) {
            this.storages[i] = new FluidStorage(capacity);
            this.storages[i].setOnContentsChanged(this::onContentChanged);
        }
    }

    public NotifiableFluidTank(MetaMachine machine, int slots, long capacity, IO io) {
        this(machine, slots, capacity, io, io);
    }

    private void onContentChanged() {
        isEmpty = null;
        updateTimeStamp(machine.getLevel());
        notifyListeners();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<FluidStack> handleRecipeInner(IO io, GTRecipe recipe, List<FluidStack> left, @Nullable String slotName, boolean simulate) {
        if (io != this.handlerIO) return left;
        for (FluidStorage capability : storages) {
            Iterator<FluidStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    FluidStack fluidStack = iterator.next();
                    if (fluidStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    boolean found = false;
                    for (int i = 0; i < capability.getTanks(); i++) {
                        FluidStack stored = capability.getFluidInTank(i);
                        if (!stored.isFluidEqual(fluidStack)) {
                            continue;
                        }
                        found = true;
                    }
                    if (!found) continue;
                    FluidStack drained = capability.drain(fluidStack.copy(), simulate);

                    fluidStack.setAmount(fluidStack.getAmount() - drained.getAmount());
                    if (fluidStack.getAmount() <= 0) {
                        iterator.remove();
                    }
                }
            } else if (io == IO.OUT) {
                while (iterator.hasNext()) {
                    FluidStack fluidStack = iterator.next();
                    if (fluidStack.isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                    long filled = capability.fill(fluidStack.copy(), simulate);
                    if (!fluidStack.isEmpty()) {
                        fluidStack.setAmount(fluidStack.getAmount() - filled);
                    }
                    if (fluidStack.getAmount() <= 0) {
                        iterator.remove();
                    }
                }
            }
            if (left.isEmpty()) return null;
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
    public RecipeCapability<FluidStack> getCapability() {
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
    public long fill(FluidStack resource, boolean simulate) {
        if (canCapInput()) {
            return fillInternal(resource, simulate);
        }
        return 0;
    }

    public long fillInternal(FluidStack resource, boolean simulate) {
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            for (var storage : storages) {
                var candidate = copied.copy();
                copied.shrink(storage.fill(candidate, simulate));
                if (copied.isEmpty()) break;
            }
            return resource.getAmount() - copied.getAmount();
        }
        return 0;
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
    public final void onContentsChanged() {
    }

}