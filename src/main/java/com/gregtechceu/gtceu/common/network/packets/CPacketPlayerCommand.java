package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.core.IGTSyncedDataPlayer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CPacketPlayerCommand implements GTNetwork.INetPacket {

    private final Action action;

    public CPacketPlayerCommand(Action action) {
        this.action = action;
    }

    public CPacketPlayerCommand(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getSender() != null) {
            switch (this.action) {
                case START_JETPACK_THRUSTING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setJetpackThrusting(true);
                case STOP_JETPACK_THRUSTING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setJetpackThrusting(false);
                case START_PRECISION_HOVERING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setPrecisionHovering(true);
                case STOP_PRECISION_HOVERING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setPrecisionHovering(false);
                case START_SAFETY_BRAKING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setSafetyBraking(true);
                case STOP_SAFETY_BRAKING -> ((IGTSyncedDataPlayer) context.getSender())
                        .gtceu$setSafetyBraking(false);
            }
        }
    }

    public enum Action {
        START_JETPACK_THRUSTING,
        STOP_JETPACK_THRUSTING,
        START_PRECISION_HOVERING,
        STOP_PRECISION_HOVERING,
        START_SAFETY_BRAKING,
        STOP_SAFETY_BRAKING;
    }
}
