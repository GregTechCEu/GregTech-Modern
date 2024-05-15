package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote FluidTankProxyTrait
 */
@Accessors(chain = true)
public class FluidTankProxyTrait extends MachineTrait implements IFluidHandler, ICapabilityTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidTankProxyTrait.class);
    @Getter
    public final IO capabilityIO;
    @Setter
    @Getter
    @Nullable
    public IFluidHandler proxy;

    public FluidTankProxyTrait(MetaMachine machine, IO capabilityIO) {
        super(machine);
        this.capabilityIO = capabilityIO;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******* Capability ********//
    //////////////////////////////////////

    @Override
    public int getTanks() {
        return proxy == null ? 0 : proxy.getTanks();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return proxy == null ? FluidStack.EMPTY : proxy.getFluidInTank(tank);
    }

    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        if (proxy != null) {
            // proxy.setFluidInTank(tank, fluidStack);
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return proxy == null ? 0 : proxy.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return proxy != null && proxy.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction simulate) {
        if (proxy != null && canCapInput()) {
            return proxy.fill(resource, simulate);
        }
        return 0;
    }

    public long fillInternal(FluidStack resource, FluidAction simulate) {
        if (proxy != null && !resource.isEmpty()) {
            return proxy.fill(resource, simulate);
        }
        return 0;
    }

    public FluidStack drainInternal(FluidStack resource, FluidAction simulate) {
        if (proxy != null && !resource.isEmpty()) {
            return proxy.drain(resource, simulate);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction simulate) {
        if (proxy != null && canCapOutput()) {
            return proxy.drain(maxDrain, simulate);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction simulate) {
        if (proxy != null && canCapOutput()) {
            return proxy.drain(resource, simulate);
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drainInternal(int maxDrain, FluidAction simulate) {
        return proxy == null ? FluidStack.EMPTY : proxy.drain(maxDrain, simulate);
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
            FluidTransferHelper.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing),
                    facing.getOpposite());
        }
    }
}
