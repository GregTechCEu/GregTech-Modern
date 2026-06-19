package com.gregtechceu.gtceu.api.machine.trait.hpca;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import lombok.Getter;

@Getter
public class HPCACoolantProviderTrait extends HPCAComponentTrait {

    private final int coolingAmount, maxCoolantPerTick;
    private final boolean isActiveCooler;

    public HPCACoolantProviderTrait(MetaMachine machine, int upkeepEUt, int maxEUt, boolean canBeDamaged,
                                    boolean allowBridging, int coolingAmount, int maxCoolantPerTick,
                                    boolean isActiveCooler) {
        super(machine, upkeepEUt, maxEUt, canBeDamaged, allowBridging);
        this.coolingAmount = coolingAmount;
        this.maxCoolantPerTick = maxCoolantPerTick;
        this.isActiveCooler = isActiveCooler;
    }
}
