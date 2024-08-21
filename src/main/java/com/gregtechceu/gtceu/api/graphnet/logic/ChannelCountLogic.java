package com.gregtechceu.gtceu.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class ChannelCountLogic extends AbstractIntLogicData<ChannelCountLogic> {

    public static final ChannelCountLogic INSTANCE = new ChannelCountLogic().setValue(1);

    public ChannelCountLogic() {
        super("ChannelCount");
    }

    @Override
    public @NotNull ChannelCountLogic getNew() {
        return new ChannelCountLogic();
    }

    @Override
    public ChannelCountLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ChannelCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
