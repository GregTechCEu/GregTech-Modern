package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.network.ModularNetwork;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record CloseAllGuisPacket() implements CustomPacketPayload {

    public static final CloseAllGuisPacket INSTANCE = new CloseAllGuisPacket();

    public static final Identifier ID = ModularUI.id("close_all_guis");
    public static final Type<CloseAllGuisPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, CloseAllGuisPacket> CODEC = StreamCodec.unit(INSTANCE);

    public void execute(IPayloadContext context) {
        ModularNetwork.get(context.flow().isClientbound())
                .closeAll(context.player(), false);
    }

    @Override
    public Type<CloseAllGuisPacket> type() {
        return TYPE;
    }
}
