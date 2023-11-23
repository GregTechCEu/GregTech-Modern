package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import lombok.Getter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OpticalComputationHatchMachine extends MultiblockPartMachine implements IOpticalComputationHatch {

    @Getter
    private final boolean isTransmitter;

    public OpticalComputationHatchMachine(IMachineBlockEntity holder, boolean isTransmitter) {
        super(holder);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controllers = getControllers();
        if (controllers.isEmpty() || controllers.stream().noneMatch(IMultiController::isFormed)) return 0;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controllers.get(0) instanceof IOpticalComputationProvider provider) {
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
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controllers = getControllers();
        if (controllers.isEmpty() || controllers.stream().noneMatch(IMultiController::isFormed)) return 0;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controllers.get(0) instanceof IOpticalComputationProvider provider) {
                return provider.getMaxCWUt(seen);
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
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        var controllers = getControllers();
        // return true here so that unlinked hatches don't cause problems in multis like the Network Switch
        if (controllers.isEmpty() || controllers.stream().noneMatch(IMultiController::isFormed)) return true;
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (controllers.get(0) instanceof IOpticalComputationProvider provider) {
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
/*
        if (tileEntity instanceof TileEntityOpticalPipe) {
            return tileEntity.getCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER, getFrontFacing().getOpposite());
        }
 */
        return null;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
