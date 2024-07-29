package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class MEStockingBusPartMachine extends MEBusPartMachine {

    public MEStockingBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }
}
