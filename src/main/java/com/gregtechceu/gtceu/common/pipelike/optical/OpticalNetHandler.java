package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.capability.IDataAccessMachine;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpticalNetHandler implements IDataAccessMachine {

    private final OpticalPipeBlockEntity pipe;
    private final Level world;
    private final Direction facing;

    @Getter
    private OpticalPipeNet net;

    public OpticalNetHandler(OpticalPipeNet net, @NotNull OpticalPipeBlockEntity pipe, @Nullable Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getLevel();
    }

    public void updateNetwork(OpticalPipeNet net) {
        this.net = net;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe) {
        var dataHatch = getDataHatch();
        if(dataHatch == null || !dataHatch.isTransmitter()) return false;
        boolean isAvailable = dataHatch.isRecipeAvailable(recipe);
        if (isAvailable) setPipesActive();
        return isAvailable;
    }

    @Override
    public void notifyListeners() {
        var dataHatch = getDataHatch();
        if(dataHatch != null && !dataHatch.isTransmitter()) dataHatch.notifyListeners();
    }

    private void setPipesActive() {
        for (BlockPos pos : net.getAllNodes().keySet()) {
            if (world.getBlockEntity(pos) instanceof OpticalPipeBlockEntity opticalPipe) {
                opticalPipe.setActive(true, 100);
            }
        }
    }

    private boolean isNetInvalidForTraversal() {
        return net == null || pipe == null || pipe.isInValid();
    }

    IOpticalDataAccessHatch getDataHatch() {
        if (isNetInvalidForTraversal()) return null;

        OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
        if (inv == null) return null;

        return inv.getDataHatch();
    }

}
