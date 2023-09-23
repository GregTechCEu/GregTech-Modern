package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public int getUpkeepEUt() {
        return 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }
}
