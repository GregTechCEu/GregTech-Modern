package com.gregtechceu.gtceu.utils.virtualregistry;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;


public abstract class VirtualEntry implements ITagSerializable<CompoundTag> {
    public static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String COLOR_KEY = "color";
    protected static final String DESC_KEY = "description";

    @Getter
    private int color = 0xFFFFFFFF;
    @Getter
    private String colorStr = DEFAULT_COLOR;
    @NotNull
    @Getter
    @Setter
    private String description = "";

    public abstract EntryTypes<? extends VirtualEntry> getType();

    public void setColor(String color) {
        this.color = parseColor(color);
        this.colorStr = color.toUpperCase();
    }

    private int parseColor(String s) {
        long t = Long.parseLong(s, 16);
        if(t > 0x7FFFFFFF) {
            t -= 0x100000000L;
        }
        return (int)t;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() && this.color == other.color;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString(COLOR_KEY, this.colorStr);

        if(description != null && !description.isEmpty()) {
            tag.putString(DESC_KEY, this.description);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setColor(tag.getString(COLOR_KEY));
        if(tag.contains(DESC_KEY))
            setDescription(tag.getString(DESC_KEY));
    }
}
