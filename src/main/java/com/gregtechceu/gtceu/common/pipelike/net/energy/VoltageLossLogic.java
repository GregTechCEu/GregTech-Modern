package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.AbstractLongLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final VoltageLossLogic INSTANCE = new VoltageLossLogic().setValue(0);

    private VoltageLossLogic() {
        super("VoltageLoss");
    }

    @Override
    public @NotNull VoltageLossLogic getNew() {
        return new VoltageLossLogic();
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
