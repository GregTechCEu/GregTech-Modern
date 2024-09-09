package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final NetLogicEntryType<VoltageLossLogic> TYPE = new NetLogicEntryType<>("VoltageLoss",
            () -> new VoltageLossLogic().setValue(0));

    private VoltageLossLogic() {
        super(TYPE);
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
