package com.gregtechceu.gtceu.api.machines;

import com.gregtechceu.gtceu.api.machines.feature.ITieredMachine;
import lombok.Getter;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote TieredMachine
 */
public class TieredMachine extends MetaMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

}
