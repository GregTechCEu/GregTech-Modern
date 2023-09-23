package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComputationProvider;

public class HPCAComputationPartMachine extends HPCAComponentPartMachine implements IHPCAComputationProvider {

    private final boolean advanced;

    public HPCAComputationPartMachine(IMachineBlockEntity metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[advanced ? GTValues.IV : GTValues.EV];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[advanced ? GTValues.ZPM : GTValues.LuV];
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return 0;
        return advanced ? 16 : 4;
    }

    @Override
    public int getCoolingPerTick() {
        return advanced ? 4 : 2;
    }

    @Override
    public boolean canBeDamaged() {
        return true;
    }
}
