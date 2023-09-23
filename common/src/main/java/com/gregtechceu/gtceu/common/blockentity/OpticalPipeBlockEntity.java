package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.pipelike.optical.*;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumMap;

public class OpticalPipeBlockEntity extends PipeBlockEntity<OpticalPipeType, OpticalPipeData> {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(OpticalPipeBlockEntity.class, PipeBlockEntity.MANAGED_FIELD_HOLDER);

    private final EnumMap<Direction, OpticalNetHandler> handlers = new EnumMap<>(Direction.class);
    // the OpticalNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IDataAccessHatch clientDataHandler = new DefaultDataHandler();
    private final IOpticalComputationProvider clientComputationHandler = new DefaultComputationHandler();
    private WeakReference<OpticalPipeNet> currentPipeNet = new WeakReference<>(null);
    private OpticalNetHandler defaultHandler;

    private int ticksActive = 0;
    @Getter
    @Persisted @DescSynced
    private boolean isActive;

    public OpticalPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }


    private void initHandlers() {
        OpticalPipeNet net = getOpticalPipeNet();
        if (net == null) return;
        for (Direction facing : Direction.values()) {
            handlers.put(facing, new OpticalNetHandler(net, this, facing));
        }
        defaultHandler = new OpticalNetHandler(net, this, null);
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
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldOpticalPipeNet worldNet = (WorldOpticalPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getPipeLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    /*
    public void transferDataFrom(IPipeNode<OpticalPipeType, OpticalPipeData> tileEntity) {
        super.transferDataFrom(tileEntity);
        if (getOpticalPipeNet() == null)
            return;
        OpticalPipeBlockEntity pipe = (OpticalPipeBlockEntity) tileEntity;
        if (!pipe.handlers.isEmpty() && pipe.defaultHandler != null) {
            // take handlers from old pipe
            handlers.clear();
            handlers.putAll(pipe.handlers);
            defaultHandler = pipe.defaultHandler;
            checkNetwork();
        } else {
            // create new handlers
            initHandlers();
        }
    }
     */

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
            TaskHandler.enqueueServerTask((ServerLevel) getLevel(), () -> {
                if (++this.ticksActive % duration == 0) {
                    this.ticksActive = 0;
                    setActive(false, -1);
                }
            }, 0);
        }

        if (stateChanged) {
            notifyBlockUpdate();
            markAsDirty();
        }
    }

    @Override
    public boolean canAttachTo(Direction side) {
        return false;
    }

    private static class DefaultDataHandler implements IDataAccessHatch {

        @Override
        public boolean isRecipeAvailable(@Nonnull GTRecipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }

    private static class DefaultComputationHandler implements IOpticalComputationProvider {

        @Override
        public int requestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
            return 0;
        }

        @Override
        public int getMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
            return 0;
        }

        @Override
        public boolean canBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
            return false;
        }
    }
}
