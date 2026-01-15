package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.pipelike.optical.*;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumMap;

public class OpticalPipeBlockEntity extends PipeBlockEntity<OpticalPipeType, OpticalPipeProperties> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(OpticalPipeBlockEntity.class,
            PipeBlockEntity.MANAGED_FIELD_HOLDER);

    private final EnumMap<Direction, OpticalNetHandler> handlers = new EnumMap<>(Direction.class);
    // the OpticalNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IDataAccessHatch clientDataHandler = new DefaultDataHandler();
    private final IOpticalComputationProvider clientComputationHandler = new DefaultComputationHandler();
    private WeakReference<OpticalPipeNet> currentPipeNet = new WeakReference<>(null);
    private OpticalNetHandler defaultHandler;

    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    private boolean isActive;

    public OpticalPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        OpticalPipeNet net = getOpticalPipeNet();
        if (net == null) return;
        for (Direction facing : GTUtil.DIRECTIONS) {
            handlers.put(facing, new OpticalNetHandler(net, this, facing));
        }
        defaultHandler = new OpticalNetHandler(net, this, null);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_DATA_ACCESS) {
            if (level.isClientSide) {
                return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(capability,
                        LazyOptional.of(() -> clientDataHandler));
            }
            if (facing != null && !isConnected(facing)) return LazyOptional.empty();
            if (handlers.isEmpty()) initHandlers();

            checkNetwork();
            return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(capability,
                    LazyOptional.of(() -> handlers.getOrDefault(facing, defaultHandler)));
        }

        if (capability == GTCapability.CAPABILITY_COMPUTATION_PROVIDER) {
            if (level.isClientSide) {
                return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(capability,
                        LazyOptional.of(() -> clientComputationHandler));
            }
            if (facing != null && !isConnected(facing)) return LazyOptional.empty();
            if (handlers.isEmpty()) initHandlers();

            checkNetwork();
            return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(capability,
                    LazyOptional.of(() -> handlers.getOrDefault(facing, defaultHandler)));
        }

        if (capability == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(capability, LazyOptional.of(this::getCoverContainer));
        }

        return super.getCapability(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            OpticalPipeNet current = getOpticalPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (OpticalNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public OpticalPipeNet getOpticalPipeNet() {
        if (level == null || level.isClientSide)
            return null;
        OpticalPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipePos()))
            return currentPipeNet; // if current net is valid and does contain position, return it
        LevelOpticalPipeNet worldNet = (LevelOpticalPipeNet) getPipeBlock()
                .getWorldPipeNet((ServerLevel) getPipeLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    @Override
    public boolean canAttachTo(Direction side) {
        return false;
    }

    @Override
    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        if (!getLevel().isClientSide && connected && !fromNeighbor) {
            // never allow more than two connections total
            if (getNumConnections() >= 2) return;

            // also check the other pipe
            BlockEntity tile = getLevel().getBlockEntity(getPipePos().relative(side));
            if (tile instanceof IPipeNode<?, ?> pipeTile &&
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
            stateChanged = true;
        } else if (!this.isActive && active) {
            this.isActive = true;
            stateChanged = true;
            TaskHandler.enqueueServerTask((ServerLevel) getLevel(), () -> setActive(false, -1), duration);
        }

        if (stateChanged) {
            notifyBlockUpdate();
            setChanged();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.handlers.clear();
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
