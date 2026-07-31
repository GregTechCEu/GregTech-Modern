package com.gregtechceu.gtceu.common.machine.trait.hpca;

import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;

import lombok.Setter;

public class HPCAComputationProviderTrait extends HPCAComponentTrait {

    public static final MachineTraitType<HPCAComputationProviderTrait> TYPE = new MachineTraitType<>(
            HPCAComputationProviderTrait.class, HPCAComponentTrait.TYPE);

    @Setter
    private int CWUPerTick, coolingPerTick;

    public HPCAComputationProviderTrait(int upkeepEUt, int maxEUt, boolean canBeDamaged,
                                        boolean allowBridging, int CWUPerTick, int coolingPerTick) {
        super(upkeepEUt, maxEUt, canBeDamaged, allowBridging);
        this.CWUPerTick = CWUPerTick;
        this.coolingPerTick = coolingPerTick;
    }

    @Override
    public MachineTraitType<HPCAComputationProviderTrait> getTraitType() {
        return TYPE;
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
