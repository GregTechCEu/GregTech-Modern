package com.lowdragmc.gtceu.api.machine;

import com.lowdragmc.gtceu.api.machine.feature.ITieredMachine;
import lombok.Getter;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote TieredMachine
 */
public class TieredMachine extends MetaMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredMachine(IMetaMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

}
