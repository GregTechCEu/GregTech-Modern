package com.gregtechceu.gtceu.utils;

import net.minecraft.util.StringRepresentable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Represents a boolean value that can be {@code true}, {@code false} or refer to a default value.
 */
public enum TriState implements StringRepresentable {

    /**
     * Represents the boolean value {@code true}.
     */
    TRUE,
    /**
     * Represents a "default" value, often used as a fallback.
     */
    DEFAULT,
    /**
     * Represents the boolean value {@code false}.
     */
    FALSE;

    public static final Codec<TriState> CODEC = Codec.either(Codec.BOOL, StringRepresentable.fromEnum(TriState::values))
            .xmap(either -> either.map(TriState::of, Function.identity()),
                    triState -> triState != DEFAULT ? Either.left(triState.isTrue()) : Either.right(DEFAULT));

    // Helper methods

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public static TriState of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static TriState of(@Nullable Boolean value) {
        return value == null ? DEFAULT : of((boolean) value);
    }

    public static TriState of(DataResult<Boolean> value) {
        if (value.result().isPresent()) return of(value.result().get());
        // throw out parsing errors and return default
        else return DEFAULT;
    }

    public @Nullable Boolean toBoolean() {
        return switch (this) {
            case TRUE -> Boolean.TRUE;
            case FALSE -> Boolean.FALSE;
            default -> null;
        };
    }

    public DataResult<Boolean> toBooleanOrError() {
        return switch (this) {
            case TRUE -> DataResult.success(Boolean.TRUE);
            case FALSE -> DataResult.success(Boolean.FALSE);
            default -> DataResult.error(() -> "Default does not have a value", Lifecycle.stable());
        };
    }

    public boolean toBooleanOrElse(final boolean other) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            default -> other;
        };
    }

    public boolean toBooleanOrElseGet(final @NotNull BooleanSupplier supplier) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            default -> supplier.getAsBoolean();
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
