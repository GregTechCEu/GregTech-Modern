package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.gui.util.TimedProgressSupplier;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ComputationProviderMachine extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider, IControllable {
    private final TimedProgressSupplier progressSupplier;
    public int allocatedCWUt=0;
    public Integer maxCWUt=null;
    public Boolean canBridge=true;
    private boolean simulate=true;

    public ComputationProviderMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.progressSupplier = new TimedProgressSupplier(200, 47, false);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if(!isWorkingEnabled())return 0;
        return allocatedCWUt(cwut,simulate);
    }

    private int allocatedCWUt(int cwut, boolean simulate) {
        int maxCWUt = getMaxCWUt();
        this.simulate=true;
        int availableCWUt = maxCWUt - this.allocatedCWUt;
        int toAllocate = Math.min(cwut, availableCWUt);
        if (!simulate) {
            this.allocatedCWUt += toAllocate;
        }
        return toAllocate;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        return maxCWUt;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        if(canBridge==null) return canBridge=customCallback("canBridge",null,true);
        return canBridge;
    }
    public void tick() {
        maxCWUt=null;
    }
}
