package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.capability.PlayerJetpackHandler;
import com.gregtechceu.gtceu.common.network.GTNetwork;

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
        if (context.getSender() == null) {
            return;
        }
        PlayerJetpackHandler jetpackHandler = GTCapabilityHelper.getPlayerJetpackHandler(context.getSender());
        if (jetpackHandler != null) {
            switch (this.action) {
                case START_JETPACK_THRUSTING -> jetpackHandler.setJetpackThrusting(true);
                case STOP_JETPACK_THRUSTING -> jetpackHandler.setJetpackThrusting(false);
                case START_PRECISION_HOVERING -> jetpackHandler.setPrecisionHovering(true);
                case STOP_PRECISION_HOVERING -> jetpackHandler.setPrecisionHovering(false);
                case START_SAFETY_BRAKING -> jetpackHandler.setSafetyBraking(true);
                case STOP_SAFETY_BRAKING -> jetpackHandler.setSafetyBraking(false);
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
