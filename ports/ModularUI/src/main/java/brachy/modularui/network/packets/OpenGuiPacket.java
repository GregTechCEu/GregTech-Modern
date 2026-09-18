package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.api.UIFactory;
import brachy.modularui.factory.GuiData;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.utils.NetworkUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenGuiPacket<T extends GuiData>(int windowId, int networkId, UIFactory<T> factory, RegistryFriendlyByteBuf data)
        implements CustomPacketPayload {

    public static final Identifier ID = ModularUI.id("open_gui");
    public static final Type<OpenGuiPacket<?>> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiPacket<?>> CODEC = StreamCodec
            .ofMember(OpenGuiPacket::encode, OpenGuiPacket::decode);

    public static <T extends GuiData> OpenGuiPacket<T> decode(RegistryFriendlyByteBuf buf) {
        int windowId = VarInt.read(buf);
        int networkId = VarInt.read(buf);
        // noinspection unchecked
        UIFactory<T> factory = (UIFactory<T>) GuiManager.getFactory(Identifier.STREAM_CODEC.decode(buf));
        RegistryFriendlyByteBuf data = buf.mui$wrapByteBuf(NetworkUtils.readByteBuf(buf));

        return new OpenGuiPacket<>(windowId, networkId, factory, data);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        VarInt.write(buf, this.windowId);
        VarInt.write(buf, this.networkId);
        Identifier.STREAM_CODEC.encode(buf, this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            GuiManager.openFromClient(this.windowId, this.networkId, this.factory, this.data, context.player());
        } else if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer serverPlayer) {
            T guiData = this.factory.readGuiData(serverPlayer, this.data);
            GuiManager.open(this.factory, guiData, serverPlayer);
        }
    }

    @Override
    public Type<OpenGuiPacket<?>> type() {
        return TYPE;
    }
}
