package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractIntLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;

public final class SuperconductorLogic extends AbstractIntLogicData<SuperconductorLogic> {

    public static final NetLogicEntryType<SuperconductorLogic> TYPE = new NetLogicEntryType<>("Superconductor", () -> new SuperconductorLogic().setValue(0));

    public SuperconductorLogic() {
        super(TYPE);
    }

    public boolean canSuperconduct(int temp) {
        return this.getValue() > temp;
    }

}
