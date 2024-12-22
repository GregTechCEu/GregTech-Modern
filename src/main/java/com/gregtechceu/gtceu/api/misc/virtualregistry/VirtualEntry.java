package com.gregtechceu.gtceu.api.misc.virtualregistry;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(chain = true)
public abstract class VirtualEntry implements INBTSerializable<CompoundTag>, ITagSerializable<CompoundTag> {

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

    public static int parseColor(String colorString) {
        colorString = formatColorString(colorString);

        if (colorString.length() > 8) {
            colorString = colorString.substring(colorString.length() - 8);
        }

        int alpha = 255;  // make alpha 100%
        int red = Integer.parseInt(colorString.substring(0, 2), 16);
        int green = Integer.parseInt(colorString.substring(2, 4), 16);
        int blue = Integer.parseInt(colorString.substring(4, 6), 16);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static @NotNull String formatColorString(String colorString) {
        return String.format("%8s", colorString).replace(' ', '0').toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() && this.color == other.color &&
                this.description.equals(other.description);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString(COLOR_KEY, this.colorStr);

        if (!description.isEmpty())
            tag.putString(DESC_KEY, this.description);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setColor(nbt.getString(COLOR_KEY));

        if (nbt.contains(DESC_KEY))
            this.description = nbt.getString(DESC_KEY);
    }

    public boolean canRemove() {
        return this.description.isEmpty();
    }
}
