package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class OpticalPipeBlockEntity extends PipeBlockEntity<OpticalPipeType> {

    // the OpticalNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IDataAccessHatch clientDataHandler = new DefaultDataHandler();
    private final IOpticalComputationProvider clientComputationHandler = new DefaultComputationHandler();

    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean isActive;

    public OpticalPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, GTPipeNetworks.OPTICAL, pos, blockState);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    public boolean canAttachTo(Direction side) {
        return false;
    }

    @Override
    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        if (!getLevel().isClientSide && connected && !fromNeighbor) {
            // never allow more than two connections total
            if (getNumConnections() >= 2) return;

            // also check the other pipe
            BlockEntity tile = getLevel().getBlockEntity(getBlockPos().relative(side));
            if (tile instanceof PipeBlockEntity<?> pipeTile &&
                    pipeTile.getPipeType().getClass() == this.getPipeType().getClass()) {
                if (pipeTile.getNumConnections() >= 2) return;
            }
        }
        super.setConnection(side, connected, fromNeighbor);
    }

    /**
     * @param active   if the pipe should become active
     * @param duration how long the pipe should be active for
     */
    public void setActive(boolean active, int duration) {
        boolean stateChanged = false;
        if (this.isActive && !active) {
            this.isActive = false;
            syncDataHolder.markClientSyncFieldDirty("isActive");
            stateChanged = true;
        } else if (!this.isActive && active) {
            this.isActive = true;
            syncDataHolder.markClientSyncFieldDirty("isActive");
            stateChanged = true;
            TaskHandler.enqueueServerTask((ServerLevel) getLevel(), () -> setActive(false, -1), duration);
        }

        if (stateChanged) {
            notifyBlockUpdate();
            setChanged();
        }
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }

    private static class DefaultDataHandler implements IDataAccessHatch {

        @Override
        public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }

    private static class DefaultComputationHandler implements IOpticalComputationProvider {

        @Override
        public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
            return 0;
        }

        @Override
        public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
            return 0;
        }

        @Override
        public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
            return false;
        }
    }
}
