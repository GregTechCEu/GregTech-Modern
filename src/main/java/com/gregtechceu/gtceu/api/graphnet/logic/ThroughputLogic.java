package com.gregtechceu.gtceu.api.graphnet.logic;

public final class ThroughputLogic extends AbstractLongLogicData<ThroughputLogic> {

    public static final NetLogicEntryType<ThroughputLogic> TYPE = new NetLogicEntryType<>("Throughput",
            () -> new ThroughputLogic().setValue(0));

    private ThroughputLogic() {
        super(TYPE);
    }

    @Override
    public ThroughputLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ThroughputLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
