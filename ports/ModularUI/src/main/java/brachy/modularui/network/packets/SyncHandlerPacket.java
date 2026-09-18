package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPacketWriter;
import brachy.modularui.core.extensions.IRegistryFriendlyByteBufExtension;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.utils.NetworkUtils;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

public record SyncHandlerPacket(int networkId, String panel, String key, boolean action,
                                @Nullable("null on the sending side") RegistryFriendlyByteBuf packet,
                                @Nullable("null on the receiving side") IPacketWriter<? super RegistryFriendlyByteBuf> packetWriter)
        implements CustomPacketPayload {

    public static final Identifier ID = ModularUI.id("sync_message");
    public static final Type<SyncHandlerPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHandlerPacket> CODEC = StreamCodec
            .ofMember(SyncHandlerPacket::encode, SyncHandlerPacket::decode);

    public SyncHandlerPacket(int networkId, String panel, String key, boolean action,
                             IPacketWriter<? super RegistryFriendlyByteBuf> packetWriter) {
        this(networkId, panel, key, action, null, packetWriter);
    }

    public SyncHandlerPacket(int networkId, String panel, String key, boolean action, RegistryFriendlyByteBuf packet) {
        this(networkId, panel, key, action, packet, null);
    }

    public static SyncHandlerPacket decode(RegistryFriendlyByteBuf buf) {
        int networkId = buf.readVarInt();
        String panel = NetworkUtils.readStringSafe(buf);
        String key = NetworkUtils.readStringSafe(buf);
        boolean action = buf.readBoolean();
        RegistryFriendlyByteBuf packet = buf.mui$wrapByteBuf(NetworkUtils.readByteBuf(buf));

        return new SyncHandlerPacket(networkId, panel, key, action, packet);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.networkId);
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, processPacketWriter(buf.registryAccess()));
    }

    private RegistryFriendlyByteBuf processPacketWriter(RegistryAccess registryAccess) {
        if (this.packet != null) {
            return packet;
        } else {
            RegistryFriendlyByteBuf buffer = IRegistryFriendlyByteBufExtension.createEmpty(registryAccess);
            if (this.packetWriter != null) {
                this.packetWriter.write(buffer);
            }
            return buffer;
        }
    }

    public void execute(IPayloadContext context) {
        ModularNetwork.get(context.flow().isClientbound())
                .receivePacket(context.player(), this);
    }

    @Override
    public Type<SyncHandlerPacket> type() {
        return TYPE;
    }
}
