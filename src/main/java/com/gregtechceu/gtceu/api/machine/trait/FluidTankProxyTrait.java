package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote FluidTankProxyTrait
 */
@Accessors(chain = true)
public class FluidTankProxyTrait extends MachineTrait implements IFluidTransfer, ICapabilityTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidTankProxyTrait.class);
    @Getter
    public final IO capabilityIO;
    @Setter @Getter @Nullable
    public IFluidTransfer proxy;

    public FluidTankProxyTrait(MetaMachine machine, IO capabilityIO) {
        super(machine);
        this.capabilityIO = capabilityIO;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    //*******     Capability    ********//
    //////////////////////////////////////

    @Override
    public void onContentsChanged() {
        if (proxy != null) proxy.onContentsChanged();
    }

    @Override
    public int getTanks() {
        return proxy == null ? 0 : proxy.getTanks();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return proxy == null ? FluidStack.empty() : proxy.getFluidInTank(tank);
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        if (proxy != null) {
            proxy.setFluidInTank(tank, fluidStack);
        }
    }

    @Override
    public long getTankCapacity(int tank) {
        return proxy == null ? 0 : proxy.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return proxy != null && proxy.isFluidValid(tank, stack);
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapInput()) {
            return proxy.fill(tank, resource, simulate, notifyChanges);
        }
        return 0;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapInput()) {
            return proxy.fill(resource, simulate, notifyChanges);
        }
        return 0;
    }

    public long fillInternal(FluidStack resource, boolean simulate) {
        if (proxy != null && !resource.isEmpty()) {
            return proxy.fill(resource, simulate);
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapOutput()) {
            return proxy.drain(tank, resource, simulate, notifyChanges);
        }
        return FluidStack.empty();
    }

    public FluidStack drainInternal(FluidStack resource, boolean simulate) {
        if (proxy != null && !resource.isEmpty()) {
            return proxy.drain(resource, simulate);
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapOutput()) {
            return proxy.drain(maxDrain, simulate, notifyChanges);
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapOutput()) {
            return proxy.drain(resource, simulate, notifyChanges);
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return proxy == null ? new Object() : proxy.createSnapshot();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (proxy != null) {
            proxy.restoreFromSnapshot(snapshot);
        }
    }

    public FluidStack drainInternal(long maxDrain, boolean simulate) {
        return proxy == null ? FluidStack.empty() : proxy.drain(maxDrain, simulate);
    }

    @Override
    public boolean supportsFill(int i) {
        return canCapInput();
    }

    @Override
    public boolean supportsDrain(int i) {
        return canCapOutput();
    }

    public boolean isEmpty() {
        if (proxy instanceof NotifiableFluidTank fluidTank) return fluidTank.isEmpty();
        boolean isEmpty = true;
        if (proxy != null) {
            for (int i = 0; i < proxy.getTanks(); i++) {
                if (!proxy.getFluidInTank(i).isEmpty()) {
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
}
