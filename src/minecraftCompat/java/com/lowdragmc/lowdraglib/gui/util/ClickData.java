package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.FriendlyByteBuf;

public class ClickData {

    public final int button;
    public final boolean isShiftClick;
    public final boolean isCtrlClick;
    public final boolean isRemote;

    public ClickData() {
        this(0, false);
    }

    public ClickData(int button, boolean isRemote) {
        this.button = button;
        this.isShiftClick = Widget.isShiftDown();
        this.isCtrlClick = Widget.isCtrlDown();
        this.isRemote = isRemote;
    }

    public void writeToBuf(FriendlyByteBuf buffer) {
        buffer.writeVarInt(button);
        buffer.writeBoolean(isShiftClick);
        buffer.writeBoolean(isCtrlClick);
        buffer.writeBoolean(isRemote);
    }

    public static ClickData readFromBuf(FriendlyByteBuf buffer) {
        return new ClickData(buffer.readVarInt(), buffer.readBoolean());
    }
}
