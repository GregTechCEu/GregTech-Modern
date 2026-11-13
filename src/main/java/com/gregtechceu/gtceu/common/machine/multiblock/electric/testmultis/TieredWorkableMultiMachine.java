package com.gregtechceu.gtceu.common.machine.multiblock.electric.testmultis;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import lombok.Getter;
import lombok.Setter;

public class TieredWorkableMultiMachine extends WorkableElectricMultiblockMachine {
    @Getter
    @Setter
    public int tier;

    public TieredWorkableMultiMachine(BlockEntityCreationInfo info) {
        super(info);
    }
}
