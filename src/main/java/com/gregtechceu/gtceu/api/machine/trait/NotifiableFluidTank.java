package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote NotifiableFluidTank
 */
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
    @Setter
    protected boolean allowSameFluids; // Can different tanks be filled with the same fluid. It should be determined
                                       // while creating tanks.
    private Boolean isEmpty;

    @Persisted
    @DescSynced
    @Getter
    protected CustomFluidTank lockedFluid = new CustomFluidTank(FluidType.BUCKET_VOLUME);

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
                                                   @Nullable String slotName, boolean simulate) {
        return handleRecipe(io, left, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    // TODO: Propagate FluidAction parameter up the caller chain
    public List<FluidIngredient> handleRecipe(IO io, List<FluidIngredient> left, FluidAction action) {
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        for (var it = left.iterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }

            var fluids = ingredient.getStacks();
            if (fluids.length == 0) {
                it.remove();
                continue;
            }

            for (CustomFluidTank storage : storages) {
                FluidStack stored = storage.getFluid();
                int transferred = 0;
                int toTransfer = ingredient.getAmount();
                if (io == IO.IN) {
                    if (!ingredient.test(stored)) continue;
                    var copy = stored.copy();
                    copy.setAmount(toTransfer);
                    transferred = storage.drain(copy, action).getAmount();
                } else { // IO.OUT
                    FluidStack output = fluids[0];
                    transferred = storage.fill(output.copy(), action);
                    // TODO: Check *all* tanks first
                    if (output.isFluidEqual(stored) && transferred < toTransfer && !allowSameFluids) return left;
                }
                ingredient.setAmount(toTransfer - transferred);
                if (ingredient.getAmount() <= 0) {
                    it.remove();
                    break;
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

    // TODO: Implement this method directly into the caller, no longer has other uses
    @Nullable
    public static List<FluidIngredient> handleIngredient(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                         boolean simulate, IO handlerIO, CustomFluidTank[] storages) {
        if (io != handlerIO) return left;
        var capabilities = simulate ?
                Arrays.stream(storages).map(CustomFluidTank::copy).toArray(CustomFluidTank[]::new) :
                storages;
        for (CustomFluidTank capability : capabilities) {
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
                    var copy = foundStack.copy();
                    copy.setAmount(fluidStack.getAmount());
                    FluidStack drained = capability.drain(copy, FluidAction.EXECUTE);

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
                    int filled = capability.fill(output.copy(), FluidAction.EXECUTE);
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
        if (this.isLocked() == locked) return;
        FluidStack fluidStack = storages[0].getFluid();
        if (locked && !fluidStack.isEmpty()) {
            this.lockedFluid.setFluid(fluidStack.copy());
            this.lockedFluid.getFluid().setAmount(1);
            onContentsChanged();
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        } else {
            this.lockedFluid.setFluid(FluidStack.EMPTY);
            setFilter(stack -> true);
            onContentsChanged();
        }
    }

    public void setLocked(boolean locked, FluidStack fluidStack) {
        if (this.isLocked() == locked) return;
        if (locked && !fluidStack.isEmpty()) {
            this.lockedFluid.setFluid(fluidStack.copy());
            this.lockedFluid.getFluid().setAmount(1);
            onContentsChanged();
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        } else {
            this.lockedFluid.setFluid(FluidStack.EMPTY);
            setFilter(stack -> true);
            onContentsChanged();
        }
    }

    public NotifiableFluidTank setFilter(Predicate<FluidStack> filter) {
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
    public List<Object> getContents() {
        List<FluidStack> ingredients = new ArrayList<>();
        for (int i = 0; i < getTanks(); ++i) {
            FluidStack stack = getFluidInTank(i);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return Arrays.asList(ingredients.toArray());
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
            GTUtil.getAdjacentFluidHandler(level, pos, facing)
                    .ifPresent(h -> FluidUtil.tryFluidTransfer(h, this, Integer.MAX_VALUE, true));
        }
    }

    public void importFromNearby(@NotNull Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            GTUtil.getAdjacentFluidHandler(level, pos, facing)
                    .ifPresent(h -> FluidUtil.tryFluidTransfer(this, h, Integer.MAX_VALUE, true));
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
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            for (var storage : storages) {
                var candidate = copied.copy();
                copied.shrink(storage.drain(candidate, action).getAmount());
                if (copied.isEmpty()) break;
            }
            copied.setAmount(resource.getAmount() - copied.getAmount());
            return copied;
        }
        return FluidStack.EMPTY;
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
        if (maxDrain == 0) {
            return FluidStack.EMPTY;
        }
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
