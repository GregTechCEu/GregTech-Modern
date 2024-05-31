package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@NoArgsConstructor
public class CPacketKeysPressed implements IPacket {

    private IntList pressedKeys;


    public CPacketKeysPressed(IntList pressedKeys) {
        this.pressedKeys = pressedKeys;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(pressedKeys.size());
        for (int key : pressedKeys) {
            buf.writeVarInt(key);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        pressedKeys = new IntArrayList();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            pressedKeys.add(buf.readVarInt());
        }
    }

    @Override
    public void execute(IHandlerContext handler) {
        KeyBind[] keyBinds = KeyBind.VALUES;
        for (int index : pressedKeys) {
            keyBinds[index].onKeyPressed(handler.getPlayer());
        }
    }
}
