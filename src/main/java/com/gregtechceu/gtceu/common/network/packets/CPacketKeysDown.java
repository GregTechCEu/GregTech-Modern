package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@NoArgsConstructor
public class CPacketKeysDown implements IPacket {
    private Int2BooleanMap updateKeys;

    public CPacketKeysDown(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.updateKeys = new Int2BooleanOpenHashMap();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys.put(buf.readVarInt(), buf.readBoolean());
        }
    }

    @Override
    public void execute(IHandlerContext handler) {
        KeyBind[] keybinds = KeyBind.VALUES;
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            keybinds[entry.getIntKey()].updateKeyDown(entry.getBooleanValue(), handler.getPlayer());
        }
    }
}
