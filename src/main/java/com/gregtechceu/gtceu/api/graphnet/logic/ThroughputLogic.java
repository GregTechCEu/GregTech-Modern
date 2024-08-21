package com.gregtechceu.gtceu.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class ThroughputLogic extends AbstractLongLogicData<ThroughputLogic> {

    public static final ThroughputLogic INSTANCE = new ThroughputLogic().setValue(0);

    private ThroughputLogic() {
        super("Throughput");
    }

    @Override
    public @NotNull ThroughputLogic getNew() {
        return new ThroughputLogic();
    }

    @Override
    public ThroughputLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ThroughputLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
