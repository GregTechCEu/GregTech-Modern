package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;

import com.mojang.datafixers.util.Pair;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class CPacketKeysPressed implements IPacket {

    private Object updateKeys;

    public CPacketKeysPressed(List<KeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        List<KeyBind> updateKeys = (List<KeyBind>) this.updateKeys;
        buf.writeVarInt(updateKeys.size());
        for (KeyBind keyBind : updateKeys) {
            buf.writeVarInt(keyBind.ordinal());
            buf.writeBoolean(keyBind.isPressed());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.updateKeys = new Pair[KeyBind.VALUES.length];
        Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = Pair.of(buf.readBoolean(), buf.readBoolean());
        }
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.getPlayer() != null) {
            KeyBind[] keybinds = KeyBind.VALUES;
            Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
            for (int i = 0; i < updateKeys.length; i++) {
                Pair<Boolean, Boolean> pair = updateKeys[i];
                if (pair != null) {
                    keybinds[i].update(pair.getFirst(), pair.getSecond(), handler.getPlayer());
                }
            }
        }
    }
}
