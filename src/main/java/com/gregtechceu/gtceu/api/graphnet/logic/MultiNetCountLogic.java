package com.gregtechceu.gtceu.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class MultiNetCountLogic extends AbstractIntLogicData<MultiNetCountLogic> {

    public static final MultiNetCountLogic INSTANCE = new MultiNetCountLogic().setValue(1);

    public MultiNetCountLogic() {
        super("MultiNetCount");
    }

    @Override
    public @NotNull MultiNetCountLogic getNew() {
        return new MultiNetCountLogic();
    }

    @Override
    public MultiNetCountLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof MultiNetCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
