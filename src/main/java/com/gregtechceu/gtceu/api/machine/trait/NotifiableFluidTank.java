package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class NotifiableFluidTank extends NotifiableRecipeHandlerTrait<FluidIngredient>
                                 implements ICapabilityTrait, IFluidHandlerModifiable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableFluidTank.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Persisted
    @Getter
    protected final CustomFluidTank[] storages;
    @Getter
    protected boolean allowSameFluids; // Can different tanks be filled with the same fluid. It should be determined
                                       // while creating tanks.
    private Boolean isEmpty;

    @Persisted
    @DescSynced
    @Getter
    protected final CustomFluidTank lockedFluid = new CustomFluidTank(FluidType.BUCKET_VOLUME);
    @Getter
    protected Predicate<FluidStack> filter = f -> true;

    public NotifiableFluidTank(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO) {
        super(machine);
        this.handlerIO = io;
        this.storages = new CustomFluidTank[slots];
        this.capabilityIO = capabilityIO;
        for (int i = 0; i < this.storages.length; i++) {
            this.storages[i] = new CustomFluidTank(capacity);
            this.storages[i].setOnContentsChanged(this::onContentsChanged);
        }
    }

    public NotifiableFluidTank(MetaMachine machine, List<CustomFluidTank> storages, IO io, IO capabilityIO) {
        super(machine);
        this.handlerIO = io;
        this.storages = storages.toArray(CustomFluidTank[]::new);
        this.capabilityIO = capabilityIO;
        for (CustomFluidTank storage : this.storages) {
            storage.setOnContentsChanged(this::onContentsChanged);
        }
        if (io == IO.IN) {
            this.allowSameFluids = true;
        }
    }

    public NotifiableFluidTank(MetaMachine machine, int slots, int capacity, IO io) {
        this(machine, slots, capacity, io, io);
    }

    public NotifiableFluidTank(MetaMachine machine, List<CustomFluidTank> storages, IO io) {
        this(machine, storages, io, io);
    }

    public void onContentsChanged() {
        isEmpty = null;
        notifyListeners();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   boolean simulate) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        // Temporarily remove listeners so that we can broadcast the entire set of transactions once
        Runnable[] listeners = new Runnable[storages.length];
        for (int i = 0; i < storages.length; i++) {
            listeners[i] = storages[i].getOnContentsChanged();
            storages[i].setOnContentsChanged(() -> {});
        }
        boolean changed = false;

        FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
        // Store the FluidStack in each slot after an operation
        // Necessary for simulation since we don't actually modify the slot's contents
        // Doesn't hurt for execution, and definitely cheaper than copying the entire storage
        FluidStack[] visited = new FluidStack[storages.length];
        for (var it = left.iterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }

            FluidStack[] fluids;

            if (ingredient instanceof IntProviderFluidIngredient provider) {
                provider.setFluidStacks(null);
                provider.setSampledCount(-1);

                if (simulate) {
                    fluids = new FluidStack[] { provider.getMaxSizeStack() };
                } else {
                    fluids = provider.getStacks();
                }
            } else {
                fluids = ingredient.getStacks();
            }
            if (fluids.length == 0 || fluids[0].isEmpty()) {
                it.remove();
                continue;
            }
            int amount = fluids[0].getAmount();

            if (io == IO.OUT && !allowSameFluids) {
                CustomFluidTank existing = null;
                int tank = 0;
                for (int i = 0; i < storages.length; ++i) {
                    var storage = storages[i];
                    if (!storage.getFluid().isEmpty() && storage.getFluid().isFluidEqual(fluids[0])) {
                        existing = storage;
                        tank = i;
                        break;
                    }
                }
                if (existing != null) {
                    FluidStack output = fluids[0].copy();
                    output.setAmount(amount);
                    int filled = existing.fill(output, action);
                    if (filled > 0) {
                        visited[tank] = output.copy();
                        // shortcut for oldAmount + filled (wow what an idea)
                        visited[tank].setAmount(existing.getFluidAmount());
                        changed = true;
                    }
                    amount -= filled;

                    if (amount > 0) ingredient.setAmount(amount);
                    else it.remove();
                    // Continue to next ingredient regardless of if we filled this ingredient completely
                    continue;
                }
            }

            for (int tank = 0; tank < storages.length; ++tank) {
                FluidStack current = visited[tank] == null ? getFluidInTank(tank) : visited[tank];
                int count = current.getAmount();

                if (io == IO.IN) {
                    if (current.isEmpty()) continue;
                    if (ingredient.test(current)) {
                        var drained = storages[tank].drain(Math.min(count, amount), action);
                        if (!drained.isEmpty()) {
                            visited[tank] = drained.copy();
                            visited[tank].setAmount(count - drained.getAmount());
                            changed = true;
                        }
                        amount -= drained.getAmount();
                    }
                } else { // IO.OUT && allow same fluids
                    FluidStack output = fluids[0].copy();
                    output.setAmount(amount);
                    if (visited[tank] == null || visited[tank].isFluidEqual(output)) {
                        if (count < storages[tank].getCapacity()) {
                            int filled = storages[tank].fill(output, action);
                            if (filled > 0) {
                                visited[tank] = output.copy();
                                visited[tank].setAmount(count + filled);
                                changed = true;
                                amount -= filled;

                                if (!allowSameFluids) {
                                    if (amount <= 0) it.remove();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (amount <= 0) {
                    it.remove();
                    break;
                }
            }
            // Modify ingredient if we didn't finish it off
            if (amount > 0) {
                ingredient.setAmount(amount);
            }
        }

        for (int i = 0; i < storages.length; i++) {
            storages[i].setOnContentsChanged(listeners[i]);
            if (changed && action.execute()) listeners[i].run();
        }

        return left.isEmpty() ? null : left;
    }

    @Override
    public boolean test(FluidIngredient ingredient) {
        return !this.isLocked() || ingredient.test(this.lockedFluid.getFluid());
    }

    @Override
    public int getPriority() {
        return !isLocked() || lockedFluid.getFluid().isEmpty() ? super.getPriority() : HIGH - getTanks();
    }

    public boolean isLocked() {
        return !lockedFluid.getFluid().isEmpty();
    }

    public void setLocked(boolean locked) {
        setLocked(locked, storages[0].getFluid());
    }

    public void setLocked(boolean locked, FluidStack fluidStack) {
        if (this.isLocked() == locked) return;
        if (locked && !fluidStack.isEmpty()) {
            this.lockedFluid.setFluid(fluidStack.copy());
            this.lockedFluid.getFluid().setAmount(1);
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        } else {
            this.lockedFluid.setFluid(FluidStack.EMPTY);
            setFilter(stack -> true);
        }
        onContentsChanged();
    }

    public NotifiableFluidTank setFilter(Predicate<FluidStack> filter) {
        this.filter = filter;
        for (CustomFluidTank storage : storages) {
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

    @Override
    public int getSize() {
        return getTanks();
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<FluidStack> ingredients = new ArrayList<>();
        for (int i = 0; i < getTanks(); ++i) {
            FluidStack stack = getFluidInTank(i);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return new ArrayList<>(ingredients);
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (int i = 0; i < getTanks(); ++i) {
            FluidStack stack = getFluidInTank(i);
            if (!stack.isEmpty()) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (CustomFluidTank storage : storages) {
                if (!storage.getFluid().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public void exportToNearby(@NotNull Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            var filter = getMachine().getFluidCapFilter(facing, IO.OUT);
            GTTransferUtils.getAdjacentFluidHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(this, adj, filter));
        }
    }

    public void importFromNearby(@NotNull Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            var filter = getMachine().getFluidCapFilter(facing, IO.IN);
            GTTransferUtils.getAdjacentFluidHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(adj, this, filter));
        }
    }

    //////////////////////////////////////
    // ******* Capability ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return storages[tank].getFluid();
    }

    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        storages[tank].setFluid(fluidStack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return storages[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return storages[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!canCapInput()) return 0;
        return fillInternal(resource, action);
    }

    public int fillInternal(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        var copied = resource.copy();
        CustomFluidTank existingStorage = null;
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
                var filled = storage.fill(copied.copy(), action);
                if (filled > 0) {
                    copied.shrink(filled);
                    if (!allowSameFluids) {
                        break;
                    }
                }
                if (copied.isEmpty()) break;
            }
        } else {
            copied.shrink(existingStorage.fill(copied.copy(), action));
        }
        return resource.getAmount() - copied.getAmount();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (canCapOutput()) {
            return drainInternal(resource, action);
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drainInternal(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;

        var copied = resource.copy();
        for (var storage : storages) {
            var candidate = copied.copy();
            copied.shrink(storage.drain(candidate, action).getAmount());
            if (copied.isEmpty()) break;
        }
        copied.setAmount(resource.getAmount() - copied.getAmount());
        return copied;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (canCapOutput()) {
            return drainInternal(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drainInternal(int maxDrain, FluidAction action) {
        if (maxDrain == 0) return FluidStack.EMPTY;
        FluidStack totalDrained = null;
        for (var storage : storages) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = storage.drain(maxDrain, action);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = storage.drain(copy, action);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.EMPTY : totalDrained;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (this.isLocked()) {
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        }
    }
}
