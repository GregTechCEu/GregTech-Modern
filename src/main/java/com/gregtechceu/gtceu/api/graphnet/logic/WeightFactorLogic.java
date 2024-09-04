package com.gregtechceu.gtceu.api.graphnet.logic;

public final class WeightFactorLogic extends AbstractDoubleLogicData<WeightFactorLogic> {

    public static final NetLogicEntryType<WeightFactorLogic> TYPE = new NetLogicEntryType<>("WeightFactor", () -> new WeightFactorLogic().setValue(0.1d));

    private WeightFactorLogic() {
        super(TYPE);
    }

    @Override
    public WeightFactorLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof WeightFactorLogic l) {
            return getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
