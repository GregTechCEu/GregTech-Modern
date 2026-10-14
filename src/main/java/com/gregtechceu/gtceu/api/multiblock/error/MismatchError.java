package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.function.Function;

public abstract class MismatchError<T> extends PatternError {

    @Getter
    private final T expected;
    @Getter
    private final T actual;
    private Function<T, String> stringify = Object::toString;

    public MismatchError(BlockPos pos, T expected, T actual) {
        super(pos);
        this.expected = expected;
        this.actual = actual;
    }

    protected void valueToString(Function<T, String> stringify) {
        this.stringify = stringify;
    }

    /// @return the lang key string used in {@link #lang()}
    protected abstract String langKey();

    /// See {@link #valueToString(Function)} to customize how the values are stringified
    /// @return a translatable component with
    /// stringified expected and actual values at a given BlockPos
    public Component lang() {
        return Component.translatable(langKey(),
                stringify.apply(expected),
                stringify.apply(actual),
                pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return parent -> parent.child(Text.of(lang()).asWidget());
    }

    protected static <T, R extends MismatchError<T>> Codec<R> makeCodec(Codec<T> typeCodec,
                                                                        Function3<BlockPos, T, T, R> constructor) {
        return RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(MismatchError::getPos),
                typeCodec.fieldOf("expected").forGetter(MismatchError::getExpected),
                typeCodec.fieldOf("actual").forGetter(MismatchError::getActual)).apply(instance, constructor));
    }
}
