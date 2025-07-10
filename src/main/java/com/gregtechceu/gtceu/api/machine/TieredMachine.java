package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;

import lombok.Getter;

public class TieredMachine extends MetaMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }
}
