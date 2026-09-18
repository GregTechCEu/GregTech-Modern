package brachy.modularui.drawable.text;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class compiles a list of objects into renderable text. The objects can be strings or any drawable.
 * The compiler will try to inline the drawables into the text according to the given maximum width.
 * Recommended usage is via {@link TextRenderer#compileAndDraw(GuiContext, List)}.
 */
public class RichTextCompiler {

    public static final RichTextCompiler INSTANCE = new RichTextCompiler();

    private static final FormattedCharSequence SPACE = FormattedCharSequence.codepoint(' ', Style.EMPTY);

    private Font fr;
    private int maxWidth;

    private List<ITextLine> lines;
    private List<Object> currentLine;
    private float x, h;
    private final LineBreakFinder lineBreakFinder = new LineBreakFinder();

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
    }

    private void compile(List<Object> raw) {
        for (Object o : raw) {
            if (o instanceof ITextLine line) {
                newLine();
                this.lines.add(line);
                continue;
            }
            Component text = null;
            if (o instanceof Component c) {
                text = c;
            } else if (o instanceof Text key) {
                if (key == Text.EMPTY) continue;
                if (key == Text.SPACE) {
                    MutableComponent mc = key.get();
                    addLineElement(SPACE);
                    this.x += this.fr.width(mc);
                    continue;
                }
                if (key == Text.LINE_FEED) {
                    newLine();
                    continue;
                }
                text = key.getFormatted();
            } else if (!(o instanceof IDrawable)) {
                text = Component.literal(o.toString());
            }
            if (text != null) {
                compileText(text);
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
            addLineElement(icon);
        }
        newLine();
    }

    private boolean iterateFormatted(String content, int skip, Style style) {
        return FontRenderHelper.iterateFormatted(content, skip, style, Style.EMPTY, this.lineBreakFinder, this.lineBreakFinder);
    }

    private void compileText(Component component) {
        component.visit((style, content) -> {
            Style subStyle = style;
            int skip = 0;
            while (this.lineBreakFinder.reset(subStyle) && !iterateFormatted(content, skip, subStyle)) {
                if (this.lineBreakFinder.nextChar == 0 && this.x > 0) {
                    // no char fits on this line -> new line and retry
                    newLine();
                    continue;
                }
                if (this.lineBreakFinder.count != 0) {
                    String sub = content.substring(skip, this.lineBreakFinder.lineBreak);
                    addLineElement(sub, subStyle, this.lineBreakFinder.lineBreakWidth);
                }
                subStyle = this.lineBreakFinder.lineBreakStyle;
                skip = this.lineBreakFinder.nextChar;
                if (skip < 0) break;
                if (!this.lineBreakFinder.styleChanged) {
                    newLine();
                }
            }
            int end = content.length();
            if (this.lineBreakFinder.isStyleChangedAtEnd()) {
                end = this.lineBreakFinder.stylePos;
            }
            addLineElement(content.substring(skip, end), subStyle, this.lineBreakFinder.width);
            return Optional.empty();
        }, Style.EMPTY);
    }

    private void newLine() {
        while (!this.currentLine.isEmpty() && this.currentLine.get(this.currentLine.size() - 1) == SPACE) {
            this.currentLine.remove(this.currentLine.size() - 1); // trims right space
            // TODO trim all left & right space
        }
        int x = (int) Math.ceil(this.x), h = (int) Math.ceil(this.h);
        this.x = 0;
        this.h = 0;
        if (!this.currentLine.isEmpty()) {
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof FormattedCharSequence fcs) {
                // simplest case: one text element
                this.lines.add(new TextLine(fcs, x));
                this.currentLine.clear();
                return;
            }
            // if all elements are text, merge them into one text element, otherwise use a composed line
            List<FormattedCharSequence> fcsList = new ArrayList<>();
            for (Object o : this.currentLine) {
                if (o instanceof FormattedCharSequence fcs) {
                    fcsList.add(fcs);
                } else {
                    this.lines.add(new ComposedLine(this.currentLine, x, h));
                    this.currentLine = new ArrayList<>();
                    return;
                }
            }
            this.lines.add(new TextLine(FormattedCharSequence.fromList(fcsList), x));
            this.currentLine.clear();
        }
    }

    private void addLineElement(IIcon icon) {
        if (icon.getWidth() > this.maxWidth) {
            ModularUI.LOGGER.warn("Icon is wider than max width");
        } else if (this.x + icon.getWidth() > this.maxWidth) {
            newLine();
        }
        this.h = Math.max(this.h, icon.getHeight());
        this.x += icon.getWidth();
        this.currentLine.add(icon);
    }

    private void addLineElement(FormattedCharSequence fcs) {
        if (this.currentLine.isEmpty() && fcs == SPACE) return;
        this.x += this.fr.width(fcs);
        this.h = Math.max(this.h, this.fr.lineHeight);
        this.currentLine.add(fcs);
    }

    private void addLineElement(String s, Style style, float width) {
        if (this.currentLine.isEmpty()) s = trimAt(s, 0);
        if (s.isEmpty()) return;
        this.h = Math.max(this.h, this.fr.lineHeight);
        this.x += width;
        //if (s.length() > 1) s = this.fr.bidirectionalShaping(s); // TODO: this turns ", " into " ," for some reason
        this.currentLine.add(FormattedCharSequence.forward(s, style));
    }

    public static String trimRight(String s) {
        int i = s.length() - 1;
        for (; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) break;
        }
        if (i < s.length() - 1) s = s.substring(0, i + 1);
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

    /**
     * Copied and edited from {@link net.minecraft.client.StringSplitter.LineBreakFinder}.
     */
    @OnlyIn(Dist.CLIENT)
    private class LineBreakFinder implements FormattedCharSink, FontRenderHelper.StyleSink {

        private float width = 0;
        private boolean styleChanged = false;
        private int lineBreak = -1;
        private Style lineBreakStyle = Style.EMPTY;
        private float lineBreakWidth = 0;
        private int lastSpace = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private float lastSpaceWidth = 0;
        private int nextChar;
        private int count;
        private int stylePos = -1;

        public boolean reset(Style style) {
            this.width = 0;
            this.lineBreak = -1;
            this.lineBreakStyle = Style.EMPTY;
            this.lineBreakWidth = 0;
            this.width = 0;
            this.lastSpace = -1;
            this.lastSpaceStyle = Style.EMPTY;
            this.lastSpaceWidth = 0;
            this.nextChar = 0;
            this.count = 0;
            this.stylePos = -1;
            return true;
        }

        @Override
        public boolean accept(int positionInCurrentSequence, @NotNull Style style, int codePoint) {
            this.styleChanged = false;
            int i = positionInCurrentSequence;
            switch (codePoint) {
                case '\n':
                    this.nextChar = i + 1;
                    return finishIteration(i, style, this.width);
                case ' ':
                    if (this.lastSpace != i - 1 && i > 0) {
                        this.lastSpace = i;
                        this.lastSpaceStyle = style;
                        this.lastSpaceWidth = this.width;
                    }
                default:
                    float f = FontRenderHelper.getCharWidth(RichTextCompiler.this.fr, codePoint, style);
                    this.width += f;
                    if (RichTextCompiler.this.x + this.width > RichTextCompiler.this.maxWidth) {
                        // line width goes beyond max width
                        if (this.nextChar == 0 && RichTextCompiler.this.x > 0) {
                            // no char fits
                            return false;
                        }
                        if (this.lastSpace < 0) {
                            // no space found, cut string right here
                            return finishIteration(i, style, this.width);
                        }
                        // go back to last space
                        this.nextChar = this.lastSpace + 1;
                        return finishIteration(this.lastSpace, this.lastSpaceStyle, this.lastSpaceWidth);
                    }
                    this.nextChar = i + Character.charCount(codePoint);
                    this.count++;
                    return true;
            }
        }

        @Override
        public boolean accept(int stylePos, int nextPos, Style style) {
            this.styleChanged = true;
            this.stylePos = stylePos;
            this.nextChar = nextPos;
            this.lineBreak = stylePos;
            this.lineBreakStyle = style;
            this.lineBreakWidth = this.width;
            return nextPos < 0;
        }

        private boolean finishIteration(int lineBreak, Style lineBreakStyle, float width) {
            this.lineBreak = lineBreak;
            this.lineBreakStyle = lineBreakStyle;
            this.lineBreakWidth = width;
            return false;
        }

        private boolean lineBreakFound() {
            return this.lineBreak != -1;
        }

        public boolean isStyleChangedAtEnd() {
            return this.styleChanged && this.nextChar < 0;
        }
    }
}
