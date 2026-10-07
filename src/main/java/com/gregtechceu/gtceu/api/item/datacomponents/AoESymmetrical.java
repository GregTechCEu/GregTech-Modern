package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

public record AoESymmetrical(int column, int row, int layer) {

    // spotless:off
    private static final Codec<AoESymmetrical> ARRAY_CODEC = Codec.INT
            .listOf()
            .comapFlatMap(list -> Util.fixedSize(list, 3)
                            .map(l -> new AoESymmetrical(l.get(0), l.get(1), l.get(2))),
                    aoe -> List.of(aoe.column, aoe.row, aoe.layer)
            );
    private static final Codec<AoESymmetrical> NAMED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("additional_columns").forGetter(AoESymmetrical::column),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("additional_rows").forGetter(AoESymmetrical::row),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("additional_layers").forGetter(AoESymmetrical::layer)
    ).apply(instance, AoESymmetrical::new));
    private static final Codec<AoESymmetrical> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("column").forGetter(AoESymmetrical::column),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("row").forGetter(AoESymmetrical::row),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("layer").forGetter(AoESymmetrical::layer)
    ).apply(instance, AoESymmetrical::new));
    public static final Codec<AoESymmetrical> CODEC = Codec.withAlternative(NAMED_CODEC, Codec.withAlternative(ARRAY_CODEC, LEGACY_CODEC));

    public static final StreamCodec<ByteBuf, AoESymmetrical> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AoESymmetrical::column,
            ByteBufCodecs.VAR_INT, AoESymmetrical::row,
            ByteBufCodecs.VAR_INT, AoESymmetrical::layer,
            AoESymmetrical::new
    );
    // spotless:on

    public static final AoESymmetrical ZERO = new AoESymmetrical(0, 0, 0);

    public boolean isZero() {
        return this == ZERO || (this.column == 0 && this.row == 0 && this.layer == 0);
    }

    public static AoESymmetrical of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? ZERO :
                new AoESymmetrical(column, row, layer);
    }

    public AoESymmetrical min(AoESymmetrical other) {
        if (this.isZero() || other.isZero()) return AoESymmetrical.ZERO;
        if (this.equals(other)) return this;

        return new AoESymmetrical(Math.min(column, other.column),
                Math.min(row, other.row),
                Math.min(layer, other.layer));
    }

    public Mutable toMutable(AoESymmetrical max) {
        return new Mutable(max.column, max.row, max.layer, this.column, this.row, this.layer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        // noinspection PatternVariableHidesField
        if (!(o instanceof AoESymmetrical(int column, int row, int layer))) {
            return false;
        }

        return this.column == column && this.row == row && this.layer == layer;
    }

    @Override
    public int hashCode() {
        int result = column;
        result = 31 * result + row;
        result = 31 * result + layer;
        return result;
    }

    @Accessors(fluent = true, chain = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Mutable {

        @Getter
        @Setter
        public int maxColumn, maxRow, maxLayer;
        @Getter
        @Setter
        public int column, row, layer;

        public Mutable increaseColumn() {
            if (column < maxColumn) {
                column++;
            }
            return this;
        }

        public Mutable increaseRow() {
            if (row < maxRow) {
                row++;
            }
            return this;
        }

        public Mutable increaseLayer() {
            if (layer < maxLayer) {
                layer++;
            }
            return this;
        }

        public Mutable decreaseColumn() {
            if (column > 0) {
                column--;
            }
            return this;
        }

        public Mutable decreaseRow() {
            if (row > 0) {
                row--;
            }
            return this;
        }

        public Mutable decreaseLayer() {
            if (layer > 0) {
                layer--;
            }
            return this;
        }

        public AoESymmetrical toImmutable() {
            return new AoESymmetrical(Math.clamp(column, 0, maxColumn),
                    Math.clamp(row, 0, maxRow),
                    Math.clamp(layer, 0, maxLayer));
        }
    }
}
