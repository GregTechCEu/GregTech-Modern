package com.gregtechceu.gtceu.api.graphnet.logic;

import com.gregtechceu.gtceu.GTCEu;
import org.jetbrains.annotations.NotNull;

public final class ThroughputLogic extends AbstractLongLogicData<ThroughputLogic> {

    public static final LongLogicType<ThroughputLogic> TYPE = new LongLogicType<>(GTCEu.MOD_ID, "Throughput",
            ThroughputLogic::new, new ThroughputLogic());

    @Override
    public @NotNull LongLogicType<ThroughputLogic> getType() {
        return TYPE;
    }

    @Override
    public ThroughputLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ThroughputLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
