package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAEmptyPartMachine extends HPCAPartMachine {
    public HPCAEmptyPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }
}
