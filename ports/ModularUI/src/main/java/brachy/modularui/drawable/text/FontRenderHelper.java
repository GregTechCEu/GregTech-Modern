package brachy.modularui.drawable.text;

import brachy.modularui.ModularUI;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.core.mixins.client.StringSplitterAccessor;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;

import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.api.distmarker.OnlyIn;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    public static int getDefaultTextHeight() {
        if (!ModularUI.isClientThread()) return 9;
        Font fr = MCHelper.getFont();
        return fr != null ? fr.lineHeight : 9;
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawComponent(Component comp, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawComponent(comp, context, x, y, width, height, widgetTheme, 1f);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawComponent(Component comp, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme, float scale) {
        Text.renderer.setAlignment(Alignment.CENTER, width, height);
        Text.renderer.setColor(widgetTheme.getTextColor());
        Text.renderer.setScale(scale);
        Text.renderer.setPos(x, y);
        Text.renderer.setShadow(widgetTheme.isTextShadow());
        Text.renderer.draw(context.getGraphics(), comp);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getCharWidth(Font font, int codePoint, Style style) {
        return getCharWidth(font.getSplitter(), codePoint, style);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getCharWidth(StringSplitter splitter, int codePoint, Style style) {
        return ((StringSplitterAccessor) splitter).getWidthProvider().getWidth(codePoint, style);
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

    public static Component getComponentFromCharSequence(FormattedCharSequence input) {
        List<MutableComponent> parts = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        MutableObject<Style> lastStyle = new MutableObject<>(Style.EMPTY);
        input.accept((pos, style, codePoint) -> {
            // if the style changed, add the built part and reset the string builder
            if (!style.equals(lastStyle.getValue())) {
                lastStyle.setValue(style);
                parts.add(Component.literal(value.toString()).setStyle(style));
                value.setLength(0);
            } else {
                value.append(codePoint);
            }
            return true;
        });
        // add the last component that will be left behind
        if (!value.isEmpty()) {
            parts.add(Component.literal(value.toString()).setStyle(lastStyle.getValue()));
        }
        // remove completely empty components
        parts.removeIf(FontRenderHelper::isEmpty);
        // no need to join completely empty or single components
        if (parts.isEmpty()) return Component.empty();
        else if (parts.size() == 1) return parts.getFirst();

        MutableComponent composite = parts.removeFirst();
        for (Component c : parts) {
            composite.append(c);
        }
        return composite;
    }

    public static boolean isEmpty(FormattedText text) {
        if (text == FormattedText.EMPTY) return true;
        // if the text has ANY content, this will return false.
        return text.visit(content -> Optional.of(false)).orElse(true);
    }

    public static FormattedCharSequence fromChars(List<TextRenderer.FormattedChar> chars) {
        int size = chars.size();
        return switch (size) {
            case 0 -> FormattedCharSequence.EMPTY;
            case 1 -> chars.getFirst().asSequence();
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

    public static String collectChars(FormattedCharSequence fcs) {
        StringBuilder str = new StringBuilder();
        fcs.accept((positionInCurrentSequence, style, codePoint) -> {
            str.appendCodePoint(codePoint);
            return true;
        });
        return str.toString();
    }

    public static List<Component> asComponents(List<String> lines) {
        return lines.stream().<Component>map(Component::literal).toList();
    }

    public interface StyleSink {

        boolean accept(int stylePos, int nextPos, Style style);
    }

    private static boolean feedChar(Style style, FormattedCharSink sink, int position, char character) {
        return Character.isSurrogate(character) ? sink.accept(position, style, 65533) : sink.accept(position, style, character);
    }

    /**
     * Copied and edited from {@link net.minecraft.util.StringDecomposer#iterateFormatted(String, int, Style, Style, FormattedCharSink)}.
     */
    public static boolean iterateFormatted(String text, int skip, Style currentStyle, Style defaultStyle,
                                           FormattedCharSink sink, StyleSink styleSink) {
        int i = text.length();
        Style style = currentStyle;
        int stylePos = skip;
        boolean styleChanged = false;

        for (int j = skip; j < i; ++j) {
            char c0 = text.charAt(j);
            if (c0 == 167) {
                if (j + 1 >= i) {
                    break;
                }

                char c1 = text.charAt(j + 1);
                ChatFormatting chatformatting = ChatFormatting.getByCode(c1);
                if (chatformatting != null) {
                    style = chatformatting == ChatFormatting.RESET ? defaultStyle : style.applyLegacyFormat(chatformatting);
                    styleChanged = true;
                }

                ++j;
                continue;
            }
            if (styleChanged) {
                if (!styleSink.accept(stylePos + 1, j, style)) return false;
                styleChanged = false;
            }
            stylePos = j;
            if (Character.isHighSurrogate(c0)) {
                if (j + 1 >= i) {
                    if (!sink.accept(j, style, 65533)) {
                        return false;
                    }
                    break;
                }

                char c2 = text.charAt(j + 1);
                if (Character.isLowSurrogate(c2)) {
                    if (!sink.accept(j, style, Character.toCodePoint(c0, c2))) {
                        return false;
                    }
                    ++j;
                } else if (!sink.accept(j, style, 65533)) {
                    return false;
                }
            } else if (!feedChar(style, sink, j, c0)) {
                return false;
            }
        }
        return !styleChanged || styleSink.accept(stylePos + 1, -1, style);
    }
}
