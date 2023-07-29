package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationProvider;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ComputationHatchPartMachine extends TieredIOPartMachine implements IOpticalComputationHatch {
    public ComputationHatchPartMachine(IMachineBlockEntity holder, IO io) {
        super(holder, GTValues.ZPM, io);
    }

    @Override
    public boolean isTransmitter() {
        return io == IO.OUT;
    }



    @Override
    public int requestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controller = getControllers().get(0);
        if (controller == null || !controller.isFormed()) return 0;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controller instanceof IOpticalComputationProvider provider) {
                return provider.requestCWUt(cwut, simulate, seen);
            } else {
                GTCEu.LOGGER.error("Computation Transmission Hatch could not request CWU/t from its controller!");
                return 0;
            }
        } else {
            // Ask the attached Transmitter hatch, if it exists
            IOpticalComputationProvider provider = getOpticalNetProvider();
            if (provider == null) return 0;
            return provider.requestCWUt(cwut, simulate, seen);
        }
    }

    @Override
    public int getMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controller = getControllers().get(0);
        if (controller == null || !controller.isFormed()) return 0;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controller instanceof IOpticalComputationProvider provider) {
                return provider.getMaxCWUt();
            } else {
                GTCEu.LOGGER.error("Computation Transmission Hatch could not get maximum CWU/t from its controller!");
                return 0;
            }
        } else {
            // Ask the attached Transmitter hatch, if it exists
            IOpticalComputationProvider provider = getOpticalNetProvider();
            if (provider == null) return 0;
            return provider.getMaxCWUt(seen);
        }
    }

    @Override
    public boolean canBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controller = getControllers().get(0);
        // return true here so that unlinked hatches don't cause problems in multis like the Network Switch
        if (controller == null || !controller.isFormed()) return true;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controller instanceof IOpticalComputationProvider provider) {
                return provider.canBridge(seen);
            } else {
                GTCEu.LOGGER.error("Computation Transmission Hatch could not test bridge status of its controller!");
                return false;
            }
        } else {
            // Ask the attached Transmitter hatch, if it exists
            IOpticalComputationProvider provider = getOpticalNetProvider();
            if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
            return provider.canBridge(seen);
        }
    }

    @Nullable
    private IOpticalComputationProvider getOpticalNetProvider() {
        BlockEntity tileEntity = getLevel().getBlockEntity(getPos().relative(getFrontFacing()));
        if (tileEntity == null) return null;

        if (tileEntity instanceof OpticalPipeBlockEntity) {
            return GTCapabilityHelper.getComputationProvider(getLevel(), tileEntity.getBlockPos(), getFrontFacing().getOpposite());
        }
        return null;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
