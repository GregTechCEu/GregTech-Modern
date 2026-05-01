package com.lowdragmc.lowdraglib.gui.editor.data;

import net.minecraft.nbt.CompoundTag;

public class Resources {

    public static Resources defaultResource() {
        return new Resources();
    }

    public static Resources fromNBT(CompoundTag tag) {
        return new Resources();
    }

    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }
}
