package com.gregtechceu.gtceu.common.machine.trait.hpca;

import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;

import lombok.Getter;

@Getter
public class HPCACoolantProviderTrait extends HPCAComponentTrait {

    public static final MachineTraitType<HPCACoolantProviderTrait> TYPE = new MachineTraitType<>(
            HPCACoolantProviderTrait.class, HPCAComponentTrait.TYPE);

    private final int coolingAmount, maxCoolantPerTick;
    private final boolean isActiveCooler;

    public HPCACoolantProviderTrait(int upkeepEUt, int maxEUt, boolean canBeDamaged,
                                    boolean allowBridging, int coolingAmount, int maxCoolantPerTick,
                                    boolean isActiveCooler) {
        super(upkeepEUt, maxEUt, canBeDamaged, allowBridging);
        this.coolingAmount = coolingAmount;
        this.maxCoolantPerTick = maxCoolantPerTick;
        this.isActiveCooler = isActiveCooler;
    }

    @Override
    public MachineTraitType<HPCACoolantProviderTrait> getTraitType() {
        return TYPE;
    }
}
