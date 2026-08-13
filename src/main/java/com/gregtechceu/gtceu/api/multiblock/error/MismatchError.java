package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.core.BlockPos;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public abstract class MismatchError<T> extends PatternError {

    @Getter
    private final T expected;
    @Getter
    private final T actual;

    public MismatchError(BlockPos pos, T expected, T actual) {
        super(pos);
        this.expected = expected;
        this.actual = actual;
    }

    protected static <T, R extends MismatchError<T>> Codec<R> makeCodec(Codec<T> typeCodec,
                                                                        Function3<BlockPos, T, T, R> function3) {
        return RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(MismatchError::getPos),
                typeCodec.fieldOf("expected").forGetter(MismatchError::getExpected),
                typeCodec.fieldOf("actual").forGetter(MismatchError::getActual)).apply(instance, function3));
    }
}
