package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.AbstractIntLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicType;
import org.jetbrains.annotations.NotNull;

public final class SuperconductorLogic extends AbstractIntLogicData<SuperconductorLogic> {

    public static final IntLogicType<SuperconductorLogic> TYPE = new IntLogicType<>(GTCEu.MOD_ID, "Superconductor",
            SuperconductorLogic::new, new SuperconductorLogic());

    @Override
    public @NotNull IntLogicType<SuperconductorLogic> getType() {
        return TYPE;
    }

    public boolean canSuperconduct(int temp) {
        return this.getValue() > temp;
    }
}
