package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.resources.ResourceLocation;

public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean doesAllowBridging() {
        return true;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[GTValues.IV];
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }
}
