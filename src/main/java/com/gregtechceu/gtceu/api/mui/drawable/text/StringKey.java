package com.gregtechceu.gtceu.api.mui.drawable.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringKey extends BaseKey {

    private final String string;
    private final Object[] args;

    public StringKey(String string) {
        this(string, null);
    }

    public StringKey(String string, @Nullable Object[] args) {
        this.string = Objects.requireNonNull(string);
        this.args = args == null || args.length == 0 ? null : args;
    }

    @Override
    public MutableComponent get() {
        return this.args == null ? Component.translatable(this.string) : Component.translatable(this.string, this.args);
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        if (this.args == null || this.args.length == 0) return super.getFormatted(parentFormatting);
        Component text = FontRenderHelper.formatArgs(this.args,
                FormattingState.merge(parentFormatting, getFormatting()), this.string, false);
        return FontRenderHelper.format(getFormatting(), parentFormatting, text);
    }
}
