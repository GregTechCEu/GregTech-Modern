package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.drawable.ITextLine;
import com.gregtechceu.gtceu.api.mui.drawable.DelegateIcon;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * This class compiles a list of objects into renderable text. The objects can be strings or any drawable.
 * The compiler will try to inline the drawables into the text according to the given maximum width.
 * Recommended usage is via {@link TextRenderer#compileAndDraw(GuiContext, List)}.
 */
public class RichTextCompiler {

    public static final RichTextCompiler INSTANCE = new RichTextCompiler();

    private Font fr;
    private int maxWidth;

    private List<ITextLine> lines;
    private List<Object> currentLine;
    private int x, h;
    private final FormattingState formatting = new FormattingState();

    public List<ITextLine> compileLines(Font fr, List<Object> raw, int maxWidth, float scale) {
        reset(fr, (int) (maxWidth / scale));
        compile(raw);
        return lines;
    }

    public void reset(Font fr, int maxWidth) {
        this.fr = fr != null ? fr : Minecraft.getInstance().font;
        this.maxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
        this.lines = new ArrayList<>();
        this.currentLine = new ArrayList<>();
        this.x = 0;
        this.h = 0;
        this.formatting.reset();
    }

    private void compile(List<Object> raw) {
        for (Object o : raw) {
            if (o instanceof ITextLine line) {
                newLine();
                this.lines.add(line);
                continue;
            }
            MutableComponent text = null;
            if (o instanceof IKey key) {
                if (key == IKey.EMPTY) continue;
                if (key == IKey.SPACE) {
                    MutableComponent mc = key.get();
                    addLineElement(mc);
                    this.x += this.fr.width(mc);
                    continue;
                }
                if (key == IKey.LINE_FEED) {
                    newLine();
                    this.formatting.reset();
                    continue;
                }
                text = key.getFormatted();
            }
            if (o instanceof MutableComponent component) {
                text = component.copy();
            } else if (!(o instanceof IDrawable)) {
                text = Component.literal(o.toString());
            }
            if (text != null) {
                if (text.getStyle() != Style.EMPTY) {
                    addLineElement(text);
                } else {
                    compileString(text.getString());
                }
                continue;
            }
            if (!(o instanceof IIcon)) {
                o = ((IDrawable) o).asIcon();// .size(fr.lineHeight);
            }
            IIcon icon = (IIcon) o;
            IIcon delegate = icon;
            if (icon instanceof DelegateIcon di) {
                delegate = di.findRootDelegate();
            }
            if (delegate instanceof Icon icon1) {
                int defaultSize = this.fr.lineHeight;
                // if (icon1.getWidth() <= 0) icon1.width(defaultSize);
                if (icon1.getHeight() <= 0) icon1.height(defaultSize);
            }
            if (icon.getWidth() > this.maxWidth) {
                GTCEu.LOGGER.warn("Icon is wider than max width");
            }
            checkNewLine(icon.getWidth());
            addLineElement(icon);
            h = Math.max(h, icon.getHeight());
            x += icon.getWidth();
        }
        newLine();
    }

    private void compileString(String text) {
        int l = text.indexOf('\n');
        int k = 0;
        do {
            // essentially splits text at \n and compiles it
            if (l < 0) l = text.length(); // no line feed, use rest of string
            String subText = text.substring(k, l);
            k = l + 1; // start next sub string here
            while (!subText.isEmpty()) {
                // how many chars fit
                int i = this.fr.getSplitter().plainIndexAtWidth(subText, this.maxWidth - this.x, Style.EMPTY);
                if (i == 0) {
                    // doesn't fit at the end of the line, try new line
                    if (this.x > 0) i = fr.getSplitter().plainIndexAtWidth(subText, this.maxWidth, Style.EMPTY);
                    if (i == 0) throw new IllegalStateException("No space for string '" + subText + "'");
                    newLine();
                } else if (i < subText.length()) {
                    // the whole string doesn't fit
                    char c = subText.charAt(i);
                    if (c != ' ' && this.x > 0) {
                        // line was split in the middle of a word, try new line
                        int j = fr.getSplitter().plainIndexAtWidth(subText, this.maxWidth, Style.EMPTY);
                        if (j < subText.length()) {
                            c = subText.charAt(j);
                            if (j > i && c == ' ') {
                                // line was split properly on a new line
                                newLine();
                            }
                        } else {
                            // the end of the line is reached
                            newLine();
                        }
                    }
                }
                // get fitting string
                String current = subText.length() <= i ? subText : trimRight(subText.substring(0, i));
                int width = this.fr.width(current);
                addLineElement(current); // add string
                this.h = Math.max(this.h, this.fr.lineHeight);
                this.x += width;
                if (subText.length() <= i) break; // sub text reached the end
                newLine(); // string was split -> line is full
                char c = subText.charAt(i);
                if (c == ' ') i++; // if was split at space then don't include it in next sub text
                subText = subText.substring(i); // set sub text to part after split
            }
            if (l < text.length() && text.charAt(l) == '\n') {
                // was split at line feed -> new line
                newLine();
            }
        } while ((l = text.indexOf('\n', k)) >= 0 || k < text.length());
        // if no line feed found, check if we are at the end of the text
    }

    private void newLine() {
        int i = this.currentLine.size() - 1;
        if (!this.currentLine.isEmpty() && this.currentLine.get(i) instanceof String s) {
            if (s.equals(" ")) {
                this.currentLine.remove(i);
            } else {
                this.currentLine.set(i, trimRight(s));
            }
        }
        if (this.currentLine.isEmpty()) {
            // lines.add(null);
        } else if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof Component c) {
            this.lines.add(new TextLine(c, this.x));
            this.currentLine.clear();
        } else if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof String s) {
            this.lines.add(new TextLine(Component.literal(s), this.x));
            this.currentLine.clear();
        } else {
            this.lines.add(new ComposedLine(this.currentLine, this.x, this.h));
            this.currentLine = new ArrayList<>();
        }
        this.x = 0;
        this.h = 0;
    }

    private void addLineElement(Object o) {
        if (o instanceof Component c2) {
            int s = this.currentLine.size();
            if (s > 0 && this.currentLine.get(s - 1) instanceof String s1) {
                // if the last element in the line is a string, merge them
                this.currentLine.set(s - 1, s1 + c2);
                return;
            }
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof Component c1) {
                // if there is already one string in the line, merge them
                this.currentLine.set(0, c1.copy().append(c2));
                return;
            }
            o = c2.copy().withStyle(this.formatting::getFormatting);
        } else if (o instanceof String s2) {
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof String s1) {
                // if there is already one string in the line, merge them
                this.currentLine.set(0, s1 + s2);
                return;
            }
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof Component c1) {
                // if there is already one string in the line, merge them
                this.currentLine.set(0, c1.copy().append(s2));
                return;
            }
            if (this.currentLine.isEmpty()) {
                // if there is currently no string, remove all whitespace from the start,
                // but don't remove any formatting before
                int l = FontRenderHelper.getFormatLength(s2, 0);
                if (l + 1 < s2.length()) {
                    o = trimAt(s2, l);
                }
            }
            Style style = this.formatting.getFormatting(Style.EMPTY);
            StringBuilder styleBuilder = new StringBuilder();
            if (style.getColor() != null) {
                int colorRGB = style.getColor().getValue();
                for (ChatFormatting legacyColor : ChatFormatting.values()) {
                    if (!legacyColor.isColor()) continue;
                    // noinspection DataFlowIssue
                    if (colorRGB != legacyColor.getColor()) continue;
                    styleBuilder.append(legacyColor);
                    break;
                }
            }
            if (style.isBold()) {
                styleBuilder.append(ChatFormatting.BOLD);
            }
            if (style.isItalic()) {
                styleBuilder.append(ChatFormatting.ITALIC);
            }
            if (style.isUnderlined()) {
                styleBuilder.append(ChatFormatting.UNDERLINE);
            }
            if (style.isStrikethrough()) {
                styleBuilder.append(ChatFormatting.STRIKETHROUGH);
            }
            if (style.isObfuscated()) {
                styleBuilder.append(ChatFormatting.OBFUSCATED);
            }
            o = styleBuilder.toString() + o;
            this.formatting.parseFrom(s2); // parse formatting from current string
        }
        if (o instanceof Component c) {
            x += fr.width(c.getString());
            h = Math.max(h, fr.lineHeight);
        }

        this.currentLine.add(o);
    }

    private void checkNewLine(int width) {
        if (this.x > 0 && this.x + width > this.maxWidth) {
            newLine();
        }
    }

    public static String trimRight(String s) {
        int i = s.length() - 1;
        for (; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) break;
        }
        if (i < s.length() - 1) s = s.substring(0, i);
        return s;
    }

    public static String trimAt(String s, int start) {
        int l = 0;
        for (int i = Math.max(0, start), n = s.length(); i < n; i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                l++;
            } else {
                break;
            }
        }
        if (l == 0) return s;
        if (start <= 0) return s.substring(l);
        return s.substring(0, start) + s.substring(start + l);
    }
}
