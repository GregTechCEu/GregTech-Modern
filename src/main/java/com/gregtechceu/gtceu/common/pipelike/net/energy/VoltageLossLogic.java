package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicType;
import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final LongLogicType<VoltageLossLogic> TYPE = new LongLogicType<>(GTCEu.MOD_ID, "VoltageLoss",
            VoltageLossLogic::new, new VoltageLossLogic());

    @Override
    public @NotNull LongLogicType<VoltageLossLogic> getType() {
        return TYPE;
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
