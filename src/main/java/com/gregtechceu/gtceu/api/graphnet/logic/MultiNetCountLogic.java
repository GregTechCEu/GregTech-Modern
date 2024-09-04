package com.gregtechceu.gtceu.api.graphnet.logic;

public final class MultiNetCountLogic extends AbstractIntLogicData<MultiNetCountLogic> {

    public static final MultiNetCountLogic INSTANCE = new MultiNetCountLogic().setValue(1);

    public MultiNetCountLogic() {
        super("MultiNetCount");
    }

    @Override
    public MultiNetCountLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof MultiNetCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
