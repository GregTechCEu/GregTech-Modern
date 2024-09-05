package com.gregtechceu.gtceu.api.graphnet.logic;

public final class ChannelCountLogic extends AbstractIntLogicData<ChannelCountLogic> {

    public static final NetLogicEntryType<ChannelCountLogic> TYPE = new NetLogicEntryType<>("ChannelCount",
            () -> new ChannelCountLogic().setValue(1));

    public ChannelCountLogic() {
        super(TYPE);
    }

    @Override
    public ChannelCountLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ChannelCountLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
