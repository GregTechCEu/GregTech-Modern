package com.gregtechceu.gtceu.common.pipelike.optical;


import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class OpticalNetHandler implements IDataAccessHatch, IOpticalComputationProvider {

    private final OpticalPipeBlockEntity pipe;
    private final Level world;
    private final Direction facing;

    private OpticalPipeNet net;

    public OpticalNetHandler(OpticalPipeNet net, @Nonnull OpticalPipeBlockEntity pipe, @Nullable Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getLevel();
    }

    public void updateNetwork(OpticalPipeNet net) {
        this.net = net;
    }

    public OpticalPipeNet getNet() {
        return net;
    }

    @Override
    public boolean isRecipeAvailable(@Nonnull GTRecipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        boolean isAvailable = traverseRecipeAvailable(recipe, seen);
        if (isAvailable) setPipesActive();
        return isAvailable;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
        int provided = traverseRequestCWUt(cwut, simulate, seen);
        if (provided > 0) setPipesActive();
        return provided;
    }

    @Override
    public int getMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
        return traverseMaxCWUt(seen);
    }

    @Override
    public boolean canBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
        return traverseCanBridge(seen);
    }

    private void setPipesActive() {
        for (BlockPos pos : net.getAllNodes().keySet()) {
            if (world.getBlockEntity(pos) instanceof OpticalPipeBlockEntity opticalPipe) {
                opticalPipe.setActive(true, 100);
            }
        }
    }

    private boolean isNetInvalidForTraversal() {
        return net == null || pipe == null || pipe.isInValid() || pipe.isBlocked(facing);
    }

    private boolean traverseRecipeAvailable(@Nonnull GTRecipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        if (isNetInvalidForTraversal()) return false;

        OpticalPipeNet.OpticalInventory inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return false;

        IOpticalDataAccessHatch hatch = inv.getDataHatch(world);
        if (hatch == null || seen.contains(hatch)) return false;

        if (hatch.isTransmitter()) {
            return hatch.isRecipeAvailable(recipe, seen);
        }
        return false;
    }

    private int traverseRequestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.requestCWUt(cwut, simulate, seen);
    }

    private int traverseMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return 0;
        return provider.getMaxCWUt(seen);
    }

    private boolean traverseCanBridge(@Nonnull Collection<IOpticalComputationProvider> seen) {
        IOpticalComputationProvider provider = getComputationProvider(seen);
        if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
        return provider.canBridge();
    }

    @Nullable
    private IOpticalComputationProvider getComputationProvider(@Nonnull Collection<IOpticalComputationProvider> seen) {
        if (isNetInvalidForTraversal()) return null;

        OpticalPipeNet.OpticalInventory inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return null;

        IOpticalComputationProvider hatch = inv.getComputationHatch(world);
        if (hatch == null || seen.contains(hatch)) return null;
        return hatch;
    }
}
