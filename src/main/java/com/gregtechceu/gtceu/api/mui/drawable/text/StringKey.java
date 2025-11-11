package com.gregtechceu.gtceu.api.mui.drawable.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class StringKey extends BaseKey {

    private final Supplier<String> string;
    private final Supplier<Object[]> args;

    public StringKey(String string) {
        this(string, null);
    }

    public StringKey(Supplier<String> stringSupplier) {
        this(stringSupplier, null);
    }

    public StringKey(String string, @Nullable Object[] args) {
        this.string = () -> Objects.requireNonNull(string);
        this.args = args == null || args.length == 0 ? null : () -> args;
    }

    public StringKey(Supplier<String> string, @Nullable Supplier<@NotNull Object[]> args) {
        this.string = Objects.requireNonNull(string);
        this.args = args;
    }

    @Override
    public MutableComponent get() {
        return this.args == null ? Component.translatable(this.string.get()) :
                Component.translatable(this.string.get(), this.args.get());
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        if (this.args == null || this.args.get().length == 0) return super.getFormatted(parentFormatting);
        Component text = FontRenderHelper.formatArgs(this.args.get(),
                FormattingState.merge(parentFormatting, getFormatting()), this.string.get(), false);
        return FontRenderHelper.format(getFormatting(), parentFormatting, text);
    }
}
