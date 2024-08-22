package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractIntLogicData;
import org.jetbrains.annotations.NotNull;

public final class SuperconductorLogic extends AbstractIntLogicData<SuperconductorLogic> {

    public static final SuperconductorLogic INSTANCE = new SuperconductorLogic().setValue(0);

    public SuperconductorLogic() {
        super("Superconductor");
    }

    public boolean canSuperconduct(int temp) {
        return this.getValue() > temp;
    }

    @Override
    public @NotNull SuperconductorLogic getNew() {
        return new SuperconductorLogic();
    }
}
