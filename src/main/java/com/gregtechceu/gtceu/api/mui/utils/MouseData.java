package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record MouseData(Dist side, int mouseButton, boolean shift, boolean ctrl, boolean alt) {

    public boolean isClient() {
        return this.side.isClient();
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.mouseButton);
        byte data = 0;
        if (this.shift) data |= 1;
        if (this.ctrl) data |= 2;
        if (this.alt) data |= 4;
        buffer.writeByte(data);
    }

    public static MouseData readPacket(FriendlyByteBuf buffer) {
        int button = buffer.readVarInt();
        byte data = buffer.readByte();
        return new MouseData(Dist.DEDICATED_SERVER, button, (data & 1) != 0, (data & 2) != 0, (data & 4) != 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static MouseData create(int mouse) {
        return new MouseData(Dist.CLIENT, mouse,
                Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
    }
}
