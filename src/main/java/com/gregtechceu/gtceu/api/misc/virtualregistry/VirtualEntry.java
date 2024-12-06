package com.gregtechceu.gtceu.api.misc.virtualregistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class VirtualEntry implements INBTSerializable<CompoundTag> {

    public static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String COLOR_KEY = "color";
    protected static final String DESC_KEY = "description";

    @Setter
    @NotNull
    private String description = "";
    private int color = 0xFFFFFFFF;
    private String colorStr = DEFAULT_COLOR;

    public abstract EntryTypes<? extends VirtualEntry> getType();

    public void setColor(String color) {
        this.color = parseColor(color);
        this.colorStr = color.toUpperCase();
    }

    public void setColor(int color) {
        setColor(Integer.toHexString(color));
    }

    private int parseColor(String s) {
        // stupid java not having actual unsigned ints
        long tmp = Long.parseLong(s, 16);
        if (tmp > 0x7FFFFFFF) {
            tmp -= 0x100000000L;
        }
        return (int) tmp;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() && this.color == other.color;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString(COLOR_KEY, this.colorStr);

        if (!description.isEmpty())
            this.description = tag.getString(DESC_KEY);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setColor(nbt.getString(COLOR_KEY));

        if (nbt.contains(DESC_KEY))
            setDescription(nbt.getString(DESC_KEY));
    }

    public abstract boolean canRemove();
}
