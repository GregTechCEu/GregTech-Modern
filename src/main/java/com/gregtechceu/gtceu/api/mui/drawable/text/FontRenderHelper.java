package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FontRenderHelper {

    private static final int min = '0', max = 'r'; // min = 48, max = 114
    // array to access text formatting by character fast
    private static final ChatFormatting[] formattingMap = new ChatFormatting[max - min + 1];

    static {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            char c = formatting.getChar();
            formattingMap[c - min] = formatting;
            if (Character.isLetter(c)) {
                formattingMap[Character.toUpperCase(c) - min] = formatting;
            }
        }
    }

    /**
     * Returns the formatting for a character with a fast array lookup.
     *
     * @param c formatting character
     * @return formatting for character or null
     */
    @Nullable
    public static ChatFormatting getForCharacter(char c) {
        if (c < min || c > max) return null;
        return formattingMap[c - min];
    }

    public static void addAfter(ChatFormatting[] state, ChatFormatting formatting, boolean removeAllOnReset) {
        if (formatting == ChatFormatting.RESET) {
            if (removeAllOnReset) Arrays.fill(state, null);
            state[0] = formatting;
            return;
        }
        // remove reset
        if (removeAllOnReset) state[6] = null;
        if (formatting.isFormat()) {
            state[formatting.ordinal() - 15] = formatting;
            return;
        }
        // color
        state[0] = formatting;
    }

    public static MutableComponent format(@Nullable FormattingState state, @Nullable FormattingState parentState,
                                          Component text) {
        if (state == null) {
            if (parentState == null) return text.copy();
            return parentState.prependText(ChatFormatting.RESET, null).append(text);
        }
        return state.prependText(ChatFormatting.RESET, parentState).append(text);
    }

    public static MutableComponent formatArgs(Object[] args, @Nullable FormattingState parentState, String text,
                                              boolean translate) {
        if (args == null || args.length == 0) return translate ? Component.translatable(text) : Component.literal(text);
        args = Arrays.copyOf(args, args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IKey key) {
                // parent format + key format + key text + parent format
                args[i] = FormattingState.appendFormat(key.getFormatted(parentState)
                        .withStyle(ChatFormatting.RESET), parentState);
            }
        }
        return translate ? Component.translatable(text, args) : Component.literal(String.format(text, args));
    }

    public static int getDefaultTextHeight() {
        Font fr = MCHelper.getFont();
        return fr != null ? fr.lineHeight : 9;
    }

    /**
     * Calculates how many formatting characters there are at the given position of the string.
     *
     * @param s     string
     * @param start starting index
     * @return amount of formatting characters at index
     */
    public static int getFormatLength(String s, int start) {
        int i = Math.max(0, start);
        int l = 0;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 167) {
                if (i + 1 >= s.length()) return l;
                if (getForCharacter(c) == null) return l;
                l += 2;
                i++;
            } else {
                return l;
            }
        }
        return l;
    }

    public static FormattedCharSequence splitAtMax(FormattedCharSequence input, float maxWidth) {
        MutableFloat cur = new MutableFloat();
        // split the string at max width.
        List<TextRenderer.FormattedChar> output = new ArrayList<>();
        input.accept((pos, style, codePoint) -> {
            if (cur.addAndGet(TextRenderer.getWidthProvider().getWidth(codePoint, style)) > maxWidth) {
                return false;
            }
            output.add(new TextRenderer.FormattedChar(codePoint, style));
            return true;
        });
        return fromChars(output);
    }

    public static boolean isEmpty(FormattedCharSequence input) {
        if (input == FormattedCharSequence.EMPTY) {
            return true;
        }
        MutableBoolean value = new MutableBoolean(true);
        input.accept((pos, style, codePoint) -> {
            value.setFalse();
            return false;
        });
        return value.isTrue();
    }

    public static FormattedText fromSequence(FormattedCharSequence input) {
        List<FormattedText> parts = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        MutableObject<Style> lastStyle = new MutableObject<>(Style.EMPTY);
        input.accept((pos, style, codePoint) -> {
            // if the style changed, add the built part and reset the string builder
            if (!style.equals(lastStyle.getValue())) {
                lastStyle.setValue(style);
                parts.add(FormattedText.of(value.toString(), style));
                value.setLength(0);
            } else {
                value.append(codePoint);
            }
            return true;
        });
        // add the last component that will be left behind
        if (!value.isEmpty()) {
            parts.add(FormattedText.of(value.toString(), lastStyle.getValue()));
        }
        // remove completely empty components even if they're not == FormattedText.EMPTY;
        parts.removeIf(FontRenderHelper::checkEmpty);
        // no need to make composites from completely empty strings or singular ones
        if (parts.isEmpty()) return FormattedText.EMPTY;
        else if (parts.size() == 1) return parts.get(0);

        return FormattedText.composite(parts);
    }

    public static boolean checkEmpty(FormattedText text) {
        if (text == FormattedText.EMPTY) return true;
        // if the text has ANY content, this will return false.
        return text.visit(content -> Optional.of(false)).orElse(true);
    }

    public static FormattedCharSequence fromChars(List<TextRenderer.FormattedChar> chars) {
        int size = chars.size();
        return switch (size) {
            case 0 -> FormattedCharSequence.EMPTY;
            case 1 -> chars.get(0).asSequence();
            default -> (sink) -> {
                for (int i = 0; i < size; i++) {
                    TextRenderer.FormattedChar ch = chars.get(i);
                    if (!sink.accept(i, ch.style(), ch.codePoint())) {
                        return false;
                    }
                }
                return true;
            };
        };
    }

    public static FormattedCharSequence substring(FormattedCharSequence str, int start) {
        return (sink) -> {
            MutableInt globalPos = new MutableInt();
            return str.accept((pos, style, codePoint) -> {
                if (globalPos.addAndGet(1) >= start) {
                    return sink.accept(pos, style, codePoint);
                }
                return true;
            });
        };
    }

    public static FormattedCharSequence substring(FormattedCharSequence str, int start, int end) {
        return (sink) -> {
            MutableInt globalPos = new MutableInt();
            return str.accept((pos, style, codePoint) -> {
                int current = globalPos.addAndGet(1);
                if (current >= end) {
                    return false;
                } else if (current >= start) {
                    return sink.accept(pos, style, codePoint);
                }
                return true;
            });
        };
    }

    public static int length(FormattedCharSequence str) {
        MutableInt length = new MutableInt();
        str.accept((positionInCurrentSequence, style, codePoint) -> {
            length.increment();
            return true;
        });
        return length.intValue();
    }

    public static List<Component> asComponents(List<String> lines) {
        return lines.stream().<Component>map(Component::literal).toList();
    }
}
