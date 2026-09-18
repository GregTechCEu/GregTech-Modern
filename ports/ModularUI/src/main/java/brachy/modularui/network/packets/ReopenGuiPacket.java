package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.network.ModularNetwork;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record ReopenGuiPacket(int networkId) implements CustomPacketPayload {

    public static final Identifier ID = ModularUI.id("reopen_gui");
    public static final Type<ReopenGuiPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, ReopenGuiPacket> CODEC = ByteBufCodecs.VAR_INT
            .map(ReopenGuiPacket::new, ReopenGuiPacket::networkId);

    public void execute(IPayloadContext context) {
        ModularNetwork.get(context.flow().isClientbound())
                .reopen(context.player(), this.networkId, false);
    }

    @Override
    public Type<ReopenGuiPacket> type() {
        return TYPE;
    }
}
