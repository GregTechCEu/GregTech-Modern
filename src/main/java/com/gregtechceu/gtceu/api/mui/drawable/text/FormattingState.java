package com.gregtechceu.gtceu.api.mui.drawable.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Unit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class FormattingState {

    private @Nullable Unit reset;
    private @Nullable TextColor color;
    private @Nullable Boolean underline;
    private @Nullable Boolean italic;
    private @Nullable Boolean bold;
    private @Nullable Boolean strikethrough;
    private @Nullable Boolean obfuscated;
    private boolean forceDefaultColor;

    public void reset() {
        this.reset = null;
        this.color = null;
        this.underline = null;
        this.italic = null;
        this.bold = null;
        this.strikethrough = null;
        this.obfuscated = null;
        this.forceDefaultColor = false;
    }

    public void add(ChatFormatting formatting, boolean removeAllOnReset) {
        if (formatting == ChatFormatting.RESET) {
            if (removeAllOnReset) reset();
            this.reset = Unit.INSTANCE;
            return;
        }
        if (formatting.isFormat()) {
            switch (formatting) {
                case UNDERLINE -> this.underline = true;
                case ITALIC -> this.italic = true;
                case BOLD -> this.bold = true;
                case STRIKETHROUGH -> this.strikethrough = true;
                case OBFUSCATED -> this.obfuscated = true;
            }
            return;
        }
        this.color = TextColor.fromLegacyFormat(formatting);
    }

    public void forceDefaultColor() {
        this.forceDefaultColor = true;
        this.color = null;
    }

    public Style getFormatting(Style style) {
        if (this.reset != null) style = Style.EMPTY;
        if (this.color != null) style.withColor(this.color);
        if (this.underline != null) style.withUnderlined(this.underline);
        if (this.italic != null) style.withItalic(this.italic);
        if (this.bold != null) style.withBold(this.bold);
        if (this.strikethrough != null) style.withStrikethrough(this.strikethrough);
        if (this.obfuscated != null) style.withObfuscated(this.obfuscated);
        return style;
    }

    public MutableComponent prependText(MutableComponent builder) {
        return prependText(builder, null);
    }

    public MutableComponent prependText(MutableComponent builder, @Nullable FormattingState fallback) {
        prependText(this, fallback, fs -> fs.reset != null ? ChatFormatting.RESET : null, builder);
        if (!this.forceDefaultColor) {
            if (this.color != null) {
                builder.withStyle(style -> style.withColor(color));
            } else if (fallback != null && !fallback.forceDefaultColor && fallback.color != null) {
                builder.withStyle(style -> style.withColor(fallback.color));
            }
        }
        prependText(fallback, ChatFormatting.UNDERLINE, fs -> fs.underline, builder);
        prependText(fallback, ChatFormatting.ITALIC, fs -> fs.italic, builder);
        prependText(fallback, ChatFormatting.BOLD, fs -> fs.bold, builder);
        prependText(fallback, ChatFormatting.STRIKETHROUGH, fs -> fs.strikethrough, builder);
        prependText(fallback, ChatFormatting.OBFUSCATED, fs -> fs.obfuscated, builder);
        return builder;
    }

    public MutableComponent prependText(ChatFormatting style, @Nullable FormattingState fallback) {
        return prependText(Component.empty().withStyle(style), fallback);
    }

    public void setFrom(FormattingState state) {
        this.reset = state.reset;
        this.color = state.color;
        this.underline = state.underline;
        this.italic = state.italic;
        this.bold = state.bold;
        this.strikethrough = state.strikethrough;
        this.obfuscated = state.obfuscated;
        this.forceDefaultColor = state.forceDefaultColor;
    }

    public void parseFrom(String text) {
        int i = -2;
        while ((i = text.indexOf(167, i + 2)) >= 0 && i < text.length() - 1) {
            ChatFormatting formatting = FontRenderHelper.getForCharacter(text.charAt(i + 1));
            if (formatting != null) add(formatting, true);
        }
    }

    public FormattingState copy() {
        FormattingState state = new FormattingState();
        state.setFrom(this);
        return state;
    }

    public FormattingState merge(FormattingState state) {
        if (state.hasReset()) {
            setFrom(state);
            return this;
        }
        if (state.color != null) this.color = state.color;
        if (state.underline != null) this.underline = state.underline;
        if (state.italic != null) this.italic = state.italic;
        if (state.bold != null) this.bold = state.bold;
        if (state.strikethrough != null) this.strikethrough = state.strikethrough;
        if (state.obfuscated != null) this.obfuscated = state.obfuscated;
        if (state.forceDefaultColor) forceDefaultColor();
        return this;
    }

    public boolean hasReset() {
        return this.reset != null;
    }

    private void prependText(@Nullable FormattingState fallback,
                             ChatFormatting format,
                             Function<FormattingState, @Nullable Boolean> getter,
                             MutableComponent builder) {
        if (getter.apply(this) == Boolean.TRUE) builder.withStyle(format);
        else if (fallback != null && getter.apply(fallback) == Boolean.TRUE) builder.withStyle(format);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reset", reset)
                .append("color", color)
                .append("underline", underline)
                .append("italic", italic)
                .append("bold", bold)
                .append("strikethrough", strikethrough)
                .append("obfuscated", obfuscated)
                .append("forceDefaultColor", forceDefaultColor)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormattingState that = (FormattingState) o;
        return forceDefaultColor == that.forceDefaultColor && reset == that.reset && color == that.color &&
                underline == that.underline && italic == that.italic && bold == that.bold &&
                strikethrough == that.strikethrough && obfuscated == that.obfuscated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reset, color, underline, italic, bold, strikethrough, obfuscated, forceDefaultColor);
    }

    private static void prependText(FormattingState state,
                                    @Nullable FormattingState fallback,
                                    Function<FormattingState, ChatFormatting> getter,
                                    MutableComponent builder) {
        if (getter.apply(state) != null) builder.withStyle(getter.apply(state));
        else if (fallback != null && getter.apply(fallback) != null) builder.withStyle(getter.apply(fallback));
    }

    public static FormattingState merge(@Nullable FormattingState state1,
                                        @Nullable FormattingState state2) {
        return merge(state1, state2, null);
    }

    public static FormattingState merge(@Nullable FormattingState state1,
                                        @Nullable FormattingState state2,
                                        @Nullable FormattingState result) {
        if (state1 == null) {
            if (state2 == null) {
                if (result == null) result = new FormattingState();
                result.reset();
                return result;
            }
            return state2;
        } else if (state2 == null) {
            return state1;
        }
        if (result == null) result = new FormattingState();
        if (result != state1) result.setFrom(state1);
        return result.merge(state2);
    }

    public static MutableComponent appendFormat(MutableComponent builder, @Nullable FormattingState state) {
        return appendFormat(builder, state, null);
    }

    public static MutableComponent appendFormat(MutableComponent builder, @Nullable FormattingState state,
                                                @Nullable FormattingState fallback) {
        if (state == null) return builder;
        return state.prependText(builder, fallback);
    }
}
