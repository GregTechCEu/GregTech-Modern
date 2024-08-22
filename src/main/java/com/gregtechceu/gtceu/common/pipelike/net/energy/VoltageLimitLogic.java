package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLimitLogic extends AbstractLongLogicData<VoltageLimitLogic> {

    public static final VoltageLimitLogic INSTANCE = new VoltageLimitLogic().setValue(0);

    private VoltageLimitLogic() {
        super("VoltageLimit");
    }

    @Override
    public @NotNull VoltageLimitLogic getNew() {
        return new VoltageLimitLogic().setValue(INSTANCE.getValue());
    }

    @Override
    public VoltageLimitLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLimitLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
