package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import lombok.NoArgsConstructor;

import java.util.List;

@Deprecated
@SuppressWarnings("unchecked")
@NoArgsConstructor
public class CPacketKeysPressed implements GTNetwork.INetPacket {

    private Object updateKeys;

    public CPacketKeysPressed(List<KeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    public CPacketKeysPressed(FriendlyByteBuf buf) {
        this.updateKeys = new BooleanBooleanPair[KeyBind.VALUES.length];
        BooleanBooleanPair[] updateKeys = (BooleanBooleanPair[]) this.updateKeys;
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = BooleanBooleanPair.of(buf.readBoolean(), buf.readBoolean());
        }
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
    public void execute(NetworkEvent.Context context) {
        if (context.getSender() != null) {
            KeyBind[] keybinds = KeyBind.VALUES;
            BooleanBooleanPair[] updateKeys = (BooleanBooleanPair[]) this.updateKeys;
            for (int i = 0; i < updateKeys.length; i++) {
                BooleanBooleanPair pair = updateKeys[i];
                if (pair != null) {
                    keybinds[i].update(pair.firstBoolean(), pair.secondBoolean(), context.getSender());
                }
            }
        }
    }
}
