package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.mojang.datafixers.util.Pair;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
public class CPacketKeysPressed implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("keys_pressed");
    public static final Type<CPacketKeysPressed> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, CPacketKeysPressed> CODEC = StreamCodec
            .ofMember(CPacketKeysPressed::encode, CPacketKeysPressed::decode);
    private Object updateKeys;

    public CPacketKeysPressed(List<KeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    public CPacketKeysPressed(Pair<Boolean, Boolean>[] updateKeys) {
        this.updateKeys = updateKeys;
    }

    public void encode(FriendlyByteBuf buf) {
        // noinspection unchecked
        List<KeyBind> updateKeys = (List<KeyBind>) this.updateKeys;
        buf.writeVarInt(updateKeys.size());
        for (KeyBind keyBind : updateKeys) {
            buf.writeVarInt(keyBind.ordinal());
            buf.writeBoolean(keyBind.isPressed());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    public static CPacketKeysPressed decode(FriendlyByteBuf buf) {
        // noinspection unchecked
        Pair<Boolean, Boolean>[] updateKeys = new Pair[KeyBind.VALUES.length];
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = Pair.of(buf.readBoolean(), buf.readBoolean());
        }
        return new CPacketKeysPressed(updateKeys);
    }

    public static void execute(CPacketKeysPressed packet, IPayloadContext handler) {
        if (handler.player() != null) {
            KeyBind[] keybinds = KeyBind.VALUES;
            // noinspection unchecked
            Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) packet.updateKeys;
            for (int i = 0; i < updateKeys.length; i++) {
                Pair<Boolean, Boolean> pair = updateKeys[i];
                if (pair != null) {
                    keybinds[i].update(pair.getFirst(), pair.getSecond(), (ServerPlayer) handler.player());
                }
            }
        }
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
