package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComputationPartMachine extends HPCAPartMachine {

    private final boolean advanced;

    public HPCAComputationPartMachine(IMachineBlockEntity holder, boolean advanced) {
        super(holder);
        this.advanced = advanced;
    }
}
