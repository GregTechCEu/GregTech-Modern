package com.gregtechceu.gtceu.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class WeightFactorLogic extends AbstractDoubleLogicData<WeightFactorLogic> {

    public static final WeightFactorLogic INSTANCE = new WeightFactorLogic().setValue(0.1d);

    private WeightFactorLogic() {
        super("WeightFactor");
    }

    @Override
    public @NotNull WeightFactorLogic getNew() {
        return new WeightFactorLogic();
    }

    @Override
    public WeightFactorLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof WeightFactorLogic l) {
            return getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
