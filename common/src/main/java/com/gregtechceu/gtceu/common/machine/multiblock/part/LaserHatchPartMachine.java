package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Setter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class LaserHatchPartMachine extends TieredIOPartMachine {

    private LaserHatchWrapper wrapper;

    public LaserHatchPartMachine(IMachineBlockEntity holder, IO io) {
        super(holder, GTValues.LuV, io);
        this.wrapper = new LaserHatchWrapper(this, null);
    }


    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        calculateLaserContainer(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        this.wrapper = new LaserHatchWrapper(this, null);
    }

    private void calculateLaserContainer(IMultiController controllerBase) {
        if (io == IO.OUT) {
            if (controllerBase instanceof ActiveTransformerMachine activeTransformer) {
                wrapper.setBufferSupplier(activeTransformer::getWrapper);
            }
        } else {
            wrapper.setBufferSupplier(this::inputContainerSupplier);
        }
    }

    private ILaserContainer inputContainerSupplier() {
        Direction side = getFrontFacing();
        Direction oppositeSide = side.getOpposite();
        return GTCapabilityHelper.getLaserContainer(getLevel(), getPos().relative(side), oppositeSide);
    }

    @Override
    public boolean canShared() {
        return false;
    }

    private static class LaserHatchWrapper extends MachineTrait implements ILaserContainer {
        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LaserHatchWrapper.class);

        @Nullable
        @Setter
        private Supplier<ILaserContainer> bufferSupplier;

        /**
         * Create a new MTE trait.
         *
         * @param metaTileEntity the MTE to reference, and add the trait to
         */
        public LaserHatchWrapper(@NotNull MetaMachine metaTileEntity, @Nullable Supplier<ILaserContainer> bufferSupplier) {
            super(metaTileEntity);
            this.bufferSupplier = bufferSupplier;
        }

        @Override
        public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.changeEnergy(amount, seen);
            }
        }

        @Override
        public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.getEnergyStored(seen);
            }
        }

        @Override
        public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.getEnergyCapacity(seen);
            }
        }

        @Nullable
        private ILaserContainer getBuffer() {
            if (bufferSupplier == null) {
                return null;
            } else {
                return bufferSupplier.get();
            }
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
