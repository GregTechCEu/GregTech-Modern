package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCACoolantProvider;

public class HPCACoolerPartMachine extends HPCAComponentPartMachine implements IHPCACoolantProvider {

    private final boolean advanced;

    public HPCACoolerPartMachine(IMachineBlockEntity metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public int getUpkeepEUt() {
        return advanced ? GTValues.VA[GTValues.IV] : 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }

    @Override
    public int getCoolingAmount() {
        return advanced ? 2 : 1;
    }

    @Override
    public boolean isActiveCooler() {
        return advanced;
    }

    @Override
    public int getMaxCoolantPerTick() {
        return advanced ? 8 : 0;
    }
}
