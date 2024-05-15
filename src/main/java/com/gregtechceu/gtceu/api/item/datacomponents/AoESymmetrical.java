package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AoESymmetrical {

    public static final Codec<AoESymmetrical> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_column").forGetter(AoESymmetrical::getMaxColumn),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_row").forGetter(AoESymmetrical::getMaxRow),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_layer").forGetter(AoESymmetrical::getMaxLayer),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("column").forGetter(AoESymmetrical::getColumn),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("row").forGetter(AoESymmetrical::getRow),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("layer").forGetter(AoESymmetrical::getLayer))
            .apply(instance, AoESymmetrical::new));
    public static final StreamCodec<ByteBuf, AoESymmetrical> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AoESymmetrical::getMaxColumn,
            ByteBufCodecs.VAR_INT, AoESymmetrical::getMaxRow,
            ByteBufCodecs.VAR_INT, AoESymmetrical::getMaxLayer,
            ByteBufCodecs.VAR_INT, AoESymmetrical::getColumn,
            ByteBufCodecs.VAR_INT, AoESymmetrical::getRow,
            ByteBufCodecs.VAR_INT, AoESymmetrical::getLayer,
            AoESymmetrical::new);

    @Getter
    public final int maxColumn, maxRow, maxLayer;
    @Getter
    public final int column, row, layer;

    private AoESymmetrical() {
        this.maxColumn = 0;
        this.maxRow = 0;
        this.maxLayer = 0;
        this.column = 0;
        this.row = 0;
        this.layer = 0;
    }

    public boolean isNone() {
        return this == NONE || (this.maxColumn == 0 && this.maxRow == 0 && this.maxLayer == 0);
    }

    private static final AoESymmetrical NONE = new AoESymmetrical();

    public static AoESymmetrical none() {
        return NONE;
    }

    public static AoESymmetrical of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? NONE :
                new AoESymmetrical(column, row, layer, column, row, layer);
    }

    public static AoESymmetrical increaseColumn(AoESymmetrical aoe) {
        int currentColumn = aoe.column;
        if (currentColumn < aoe.maxColumn) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, currentColumn + 1, aoe.row, aoe.layer);
        }
        return aoe;
    }

    public static AoESymmetrical increaseRow(AoESymmetrical aoe) {
        int currentRow = aoe.row;
        if (currentRow < aoe.maxRow) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, aoe.column, currentRow + 1, aoe.layer);
        }
        return aoe;
    }

    public static AoESymmetrical increaseLayer(AoESymmetrical aoe) {
        int currentLayer = aoe.layer;
        if (currentLayer < aoe.maxLayer) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, aoe.column, aoe.row, currentLayer + 1);
        }
        return aoe;
    }

    public static AoESymmetrical decreaseColumn(AoESymmetrical aoe) {
        int currentColumn = aoe.column;
        if (currentColumn > 0) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, currentColumn - 1, aoe.row, aoe.layer);
        }
        return aoe;
    }

    public static AoESymmetrical decreaseRow(AoESymmetrical aoe) {
        int currentRow = aoe.row;
        if (currentRow > 0) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, aoe.column, currentRow - 1, aoe.layer);
        }
        return aoe;
    }

    public static AoESymmetrical decreaseLayer(AoESymmetrical aoe) {
        int currentLayer = aoe.layer;
        if (currentLayer > 0) {
            aoe = new AoESymmetrical(aoe.maxColumn, aoe.maxRow, aoe.maxLayer, aoe.column, aoe.row, currentLayer - 1);
        }
        return aoe;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AoESymmetrical that))
            return false;

        return maxColumn == that.maxColumn && maxRow == that.maxRow && maxLayer == that.maxLayer &&
                column == that.column && row == that.row && layer == that.layer;
    }

    @Override
    public int hashCode() {
        int result = maxColumn;
        result = 31 * result + maxRow;
        result = 31 * result + maxLayer;
        result = 31 * result + column;
        result = 31 * result + row;
        result = 31 * result + layer;
        return result;
    }
}
