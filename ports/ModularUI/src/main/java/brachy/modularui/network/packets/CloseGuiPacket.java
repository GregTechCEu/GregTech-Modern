package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.network.ModularNetwork;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record CloseGuiPacket(int networkId, boolean dispose) implements CustomPacketPayload {

    // @formatter:off
    public static final Identifier ID = ModularUI.id("close_gui");
    public static final Type<CloseGuiPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, CloseGuiPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CloseGuiPacket::networkId,
            ByteBufCodecs.BOOL, CloseGuiPacket::dispose,
            CloseGuiPacket::new
    );
    // @formatter:on

    public void execute(IPayloadContext context) {
        ModularNetwork.get(context.flow().isClientbound())
                .closeContainer(this.networkId, this.dispose, context.player(), false);
    }

    @Override
    public Type<CloseGuiPacket> type() {
        return TYPE;
    }
}
