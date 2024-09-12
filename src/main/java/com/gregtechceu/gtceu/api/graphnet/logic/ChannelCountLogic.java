package com.gregtechceu.gtceu.api.graphnet.logic;

import com.gregtechceu.gtceu.GTCEu;

import org.jetbrains.annotations.NotNull;

public final class ChannelCountLogic extends AbstractIntLogicData<ChannelCountLogic> {

    public static final IntLogicType<ChannelCountLogic> TYPE = new IntLogicType<>(GTCEu.MOD_ID, "ChannelCount",
            ChannelCountLogic::new, new ChannelCountLogic().setValue(1));

    @Override
    public @NotNull IntLogicType<ChannelCountLogic> getType() {
        return TYPE;
    }

    @Override
    public ChannelCountLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ChannelCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
