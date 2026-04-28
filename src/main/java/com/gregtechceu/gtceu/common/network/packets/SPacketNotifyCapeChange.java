package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
public class SPacketNotifyCapeChange implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("cape_changed");
    public static final Type<SPacketNotifyCapeChange> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketNotifyCapeChange> CODEC = StreamCodec
            .ofMember(SPacketNotifyCapeChange::encode, SPacketNotifyCapeChange::new);

    private final UUID uuid;
    private final Identifier cape;

    public SPacketNotifyCapeChange(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        cape = buf.readBoolean() ? buf.readIdentifier() : null;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeBoolean(this.cape != null);
        if (this.cape != null) {
            buf.writeIdentifier(this.cape);
        }
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            CapeRegistry.giveRawCape(uuid, cape);
        }
    }

    @Override
    public @NotNull Type<SPacketNotifyCapeChange> type() {
        return TYPE;
    }
}
