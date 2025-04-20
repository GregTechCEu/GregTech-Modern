package com.gregtechceu.gtceu.api.item.tool.aoe;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public class AoESymmetrical {

    public final int column, row, layer;

    private AoESymmetrical() {
        this.column = 0;
        this.row = 0;
        this.layer = 0;
    }

    private AoESymmetrical(int column, int row, int layer) {
        this.column = column;
        this.row = row;
        this.layer = layer;
    }

    public static final AoESymmetrical ZERO = new AoESymmetrical();

    public boolean isZero() {
        return this == ZERO;
    }

    public static AoESymmetrical of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? ZERO : new AoESymmetrical(column, row, layer);
    }

    public static AoESymmetrical readMax(CompoundTag tag) {
        int column = 0, row = 0, layer = 0;
        if (tag.contains(ToolHelper.MAX_AOE_COLUMN_KEY, Tag.TAG_INT)) {
            column = tag.getInt(ToolHelper.MAX_AOE_COLUMN_KEY);
        }
        if (tag.contains(ToolHelper.MAX_AOE_ROW_KEY, Tag.TAG_INT)) {
            row = tag.getInt(ToolHelper.MAX_AOE_ROW_KEY);
        }
        if (tag.contains(ToolHelper.MAX_AOE_LAYER_KEY, Tag.TAG_INT)) {
            layer = tag.getInt(ToolHelper.MAX_AOE_LAYER_KEY);
        }
        return column == 0 && row == 0 && layer == 0 ? ZERO : AoESymmetrical.of(column, row, layer);
    }

    public static AoESymmetrical read(CompoundTag tag, @Nullable AoESymmetrical defaultDefinition) {
        int column, row, layer;
        if (tag.contains(ToolHelper.AOE_COLUMN_KEY, Tag.TAG_INT)) {
            column = tag.getInt(ToolHelper.AOE_COLUMN_KEY);
        } else {
            column = defaultDefinition == null ? 0 : defaultDefinition.column;
        }
        if (tag.contains(ToolHelper.AOE_ROW_KEY, Tag.TAG_INT)) {
            row = tag.getInt(ToolHelper.AOE_ROW_KEY);
        } else {
            row = defaultDefinition == null ? 0 : defaultDefinition.row;
        }
        if (tag.contains(ToolHelper.AOE_LAYER_KEY, Tag.TAG_INT)) {
            layer = tag.getInt(ToolHelper.AOE_LAYER_KEY);
        } else {
            layer = defaultDefinition == null ? 0 : defaultDefinition.layer;
        }
        if (column == 0 && row == 0 && layer == 0) {
            return ZERO;
        }
        tag.putInt(ToolHelper.AOE_COLUMN_KEY, column);
        tag.putInt(ToolHelper.AOE_ROW_KEY, row);
        tag.putInt(ToolHelper.AOE_LAYER_KEY, layer);
        return AoESymmetrical.of(column, row, layer);
    }

    public static int getColumn(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (tag.contains(ToolHelper.AOE_COLUMN_KEY, Tag.TAG_INT)) {
            return tag.getInt(ToolHelper.AOE_COLUMN_KEY);
        }
        return defaultDefinition.column;
    }

    public static int getRow(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (tag.contains(ToolHelper.AOE_ROW_KEY, Tag.TAG_INT)) {
            return tag.getInt(ToolHelper.AOE_ROW_KEY);
        }
        return defaultDefinition.row;
    }

    public static int getLayer(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (tag.contains(ToolHelper.AOE_LAYER_KEY, Tag.TAG_INT)) {
            return tag.getInt(ToolHelper.AOE_LAYER_KEY);
        }
        return defaultDefinition.layer;
    }

    public static void increaseColumn(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_COLUMN_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_COLUMN_KEY, defaultDefinition.column);
        } else {
            int currentColumn = tag.getInt(ToolHelper.AOE_COLUMN_KEY);
            if (currentColumn < defaultDefinition.column) {
                tag.putInt(ToolHelper.AOE_COLUMN_KEY, currentColumn + 1);
            }
        }
    }

    public static void increaseRow(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_ROW_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_ROW_KEY, defaultDefinition.row);
        } else {
            int currentRow = tag.getInt(ToolHelper.AOE_ROW_KEY);
            if (currentRow < defaultDefinition.row) {
                tag.putInt(ToolHelper.AOE_ROW_KEY, currentRow + 1);
            }
        }
    }

    public static void increaseLayer(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_LAYER_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_LAYER_KEY, defaultDefinition.layer);
        } else {
            int currentLayer = tag.getInt(ToolHelper.AOE_LAYER_KEY);
            if (currentLayer < defaultDefinition.layer) {
                tag.putInt(ToolHelper.AOE_LAYER_KEY, currentLayer + 1);
            }
        }
    }

    public static void decreaseColumn(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_COLUMN_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_COLUMN_KEY, defaultDefinition.column);
        } else {
            int currentColumn = tag.getInt(ToolHelper.AOE_COLUMN_KEY);
            if (currentColumn > 0) {
                tag.putInt(ToolHelper.AOE_COLUMN_KEY, currentColumn - 1);
            }
        }
    }

    public static void decreaseRow(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_ROW_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_ROW_KEY, defaultDefinition.row);
        } else {
            int currentRow = tag.getInt(ToolHelper.AOE_ROW_KEY);
            if (currentRow > 0) {
                tag.putInt(ToolHelper.AOE_ROW_KEY, currentRow - 1);
            }
        }
    }

    public static void decreaseLayer(CompoundTag tag, AoESymmetrical defaultDefinition) {
        if (!tag.contains(ToolHelper.AOE_LAYER_KEY, Tag.TAG_INT)) {
            tag.putInt(ToolHelper.AOE_LAYER_KEY, defaultDefinition.layer);
        } else {
            int currentLayer = tag.getInt(ToolHelper.AOE_LAYER_KEY);
            if (currentLayer > 0) {
                tag.putInt(ToolHelper.AOE_LAYER_KEY, currentLayer - 1);
            }
        }
    }
}
