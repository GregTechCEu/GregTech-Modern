package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;

public final class VoltageLimitLogic extends AbstractLongLogicData<VoltageLimitLogic> {

    public static final NetLogicEntryType<VoltageLimitLogic> TYPE = new NetLogicEntryType<>("VoltageLimit", () -> new VoltageLimitLogic().setValue(0));

    private VoltageLimitLogic() {
        super(TYPE);
    }

    @Override
    public VoltageLimitLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLimitLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
