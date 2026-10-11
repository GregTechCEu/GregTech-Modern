package com.gregtechceu.gtceu.api.item.jetpack;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;

import org.jetbrains.annotations.Nullable;

public class ElectricJetpackLogic extends AbstractJetpackLogic {

    public ElectricJetpackLogic(JetpackStats stats, @Nullable JetpackStats hoverStats) {
        super(stats, hoverStats);
    }

    @Override
    public boolean canUseEnergy(JetpackUseContext context, int amount) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(context.stack());
        return electricItem != null && electricItem.canUse(amount);
    }

    @Override
    public void drainEnergy(JetpackUseContext context, int amount) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(context.stack());
        if (electricItem != null) {
            electricItem.discharge(amount, electricItem.getTier(), true, false, false);
        }
    }
}
