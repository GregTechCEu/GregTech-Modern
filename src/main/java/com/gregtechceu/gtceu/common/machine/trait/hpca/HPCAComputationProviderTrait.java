package com.gregtechceu.gtceu.common.machine.trait.hpca;

import lombok.Setter;

public class HPCAComputationProviderTrait extends HPCAComponentTrait {

    @Setter
    private int CWUPerTick, coolingPerTick;

    public HPCAComputationProviderTrait(int upkeepEUt, int maxEUt, boolean canBeDamaged,
                                        boolean allowBridging, int CWUPerTick, int coolingPerTick) {
        super(upkeepEUt, maxEUt, canBeDamaged, allowBridging);
        this.CWUPerTick = CWUPerTick;
        this.coolingPerTick = coolingPerTick;
    }

    public int getCoolingPerTick() {
        if (isDamaged()) return 0;
        return coolingPerTick;
    }

    public int getCWUPerTick() {
        if (isDamaged()) return 0;
        return CWUPerTick;
    }
}
