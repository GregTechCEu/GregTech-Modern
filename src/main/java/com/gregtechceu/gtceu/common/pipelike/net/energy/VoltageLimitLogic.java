package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLimitLogic extends AbstractLongLogicData<VoltageLimitLogic> {

    public static final LongLogicType<VoltageLimitLogic> TYPE = new LongLogicType<>(GTCEu.MOD_ID, "VoltageLimit",
            VoltageLimitLogic::new, new VoltageLimitLogic());

    @Override
    public @NotNull LongLogicType<VoltageLimitLogic> getType() {
        return TYPE;
    }

    @Override
    public VoltageLimitLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLimitLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
