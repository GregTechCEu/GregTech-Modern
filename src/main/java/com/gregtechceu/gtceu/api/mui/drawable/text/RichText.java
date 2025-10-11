package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.*;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RichText implements IDrawable, IRichTextBuilder<RichText> {

    private static final TextRenderer renderer = new TextRenderer();

    private final List<Object> elements = new ArrayList<>();
    @Getter
    private Alignment alignment = Alignment.CenterLeft;
    @Getter
    private float scale = 1f;
    @Getter
    private Integer color = null;
    @Getter
    private Boolean shadow = null;

    private int cursor = 0;
    private boolean cursorLocked = false;
    private List<ITextLine> cachedText;

    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public List<FormattedText> getStringRepresentation() {
        List<FormattedText> list = new ArrayList<>();
        for (Object o : this.elements) {
            if (o == IKey.LINE_FEED) {
                list.add(Component.empty());
                continue;
            }
            if (o instanceof IKey key) {
                list.add(key.get());
            } else if (o instanceof String s1) {
                list.add(Component.literal(s1));
            } else if (o instanceof TextIcon ti) {
                list.add(ti.getText());
            } else if (o instanceof Component c) {
                list.add(c);
            }
        }
        if (!list.isEmpty() && list.get(list.size() - 1).getString().isEmpty()) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    public int getMinWidth() {
        int minWidth = 12;
        for (Object o : this.elements) {
            if (o instanceof IIcon icon) {
                minWidth = Math.max(minWidth, icon.getWidth());
            }
        }
        return minWidth;
    }

    private void addElement(Object o) {
        this.elements.add(this.cursor, o);
        if (!this.cursorLocked) {
            this.cursor++;
        }
    }

    @Override
    public RichText getThis() {
        return this;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return this;
    }

    @Override
    public RichText add(Component c) {
        addElement(c);
        return this;
    }

    @Override
    public RichText add(String s) {
        addElement(s);
        return this;
    }

    @Override
    public RichText add(IDrawable drawable) {
        Object o = drawable;
        if (!(o instanceof IKey) && !(o instanceof IIcon)) o = drawable.asIcon();
        addElement(o);
        return this;
    }

    @Override
    public RichText addLine(ITextLine line) {
        addElement(line);
        return this;
    }

    @Override
    public RichText clearText() {
        this.elements.clear();
        this.cursor = 0;
        return this;
    }

    @Override
    public RichText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public RichText textColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public RichText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public RichText textShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public RichText moveCursorAfterElement(Pattern regex) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i < 0) i = this.elements.size();
        this.cursor = i;
        return this;
    }

    @Override
    public RichText replace(Pattern regex, UnaryOperator<IKey> function) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i >= 0) {
            this.cursor = i;
            Object o = this.elements.get(i);
            IKey key = o instanceof IKey key1 ? key1 : IKey.str((String) o);
            key = function.apply(key);
            if (key == null) {
                this.elements.remove(i);
                this.cursor--;
            } else {
                this.elements.set(i, key);
            }
        }
        return this;
    }

    @Override
    public RichText moveCursorToStart() {
        this.cursor = 0;
        return this;
    }

    @Override
    public RichText moveCursorToEnd() {
        this.cursor = this.elements.size() - 1;
        return this;
    }

    @Override
    public RichText moveCursorForward(int by) {
        this.cursor = Math.min(this.cursor + by, this.elements.size() - 1);
        return this;
    }

    @Override
    public RichText moveCursorBackward(int by) {
        this.cursor = Math.max(0, this.cursor - by);
        return this;
    }

    @Override
    public RichText lockCursor() {
        this.cursorLocked = true;
        return this;
    }

    @Override
    public RichText unlockCursor() {
        this.cursorLocked = false;
        return this;
    }

    @Override
    public RichText moveCursorToNextLine() {
        if (this.cursor < this.elements.size() - 1) {
            this.cursor = findNextLine(this.cursor) + 1;
        }
        return this;
    }

    private int findNextLine(int current) {
        for (int i = current; i < this.elements.size(); i++) {
            Object o = this.elements.get(i);
            if (o == IKey.LINE_FEED) return i;
            if (o instanceof IKey key && key.get().getString().trim().endsWith("\n")) return i;
            if (o instanceof String string && string.trim().endsWith("\n")) return i;
            if (o instanceof ITextLine) return i;
        }
        return this.elements.size() - 1;
    }

    private int findNextText(int current, boolean wrapAround, Predicate<String> test) {
        int i = current;
        int lim = this.elements.size();
        while (i < lim) {
            Object o = this.elements.get(i);
            if (o instanceof IKey key && test.test(key.get().getString())) return i;
            if (o instanceof String string && test.test(string)) return i;
            if (++i == lim && wrapAround) {
                i = 0;
                lim = current;
                wrapAround = false;
            }
        }
        return -1;
    }

    public RichText insertTitleMargin(int margin) {
        List<Object> objects = this.elements;
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (o == IKey.LINE_FEED) {
                if (i == objects.size() - 1) return this;
                if (objects.get(i + 1) instanceof Spacer spacer) {
                    if (spacer.getSpace() == margin) return this;
                    objects.set(i + 1, Spacer.of(margin));
                } else {
                    objects.add(i + 1, Spacer.of(margin));
                }
                return this;
            }
        }
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        draw(context, x, y, width, height, widgetTheme.getTextColor(), widgetTheme.getTextShadow());
    }

    public void draw(GuiContext context, int x, int y, int width, int height, int color, boolean shadow) {
        renderer.setSimulate(false);
        setupRenderer(renderer, x, y, width, height, color, shadow);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
    }

    public void setupRenderer(TextRenderer renderer, int x, int y, float width, float height, int color,
                              boolean shadow) {
        renderer.setPos(x, y);
        renderer.setScale(this.scale);
        renderer.setColor(this.color != null ? this.color : color);
        renderer.setShadow(this.shadow != null ? this.shadow : shadow);
        renderer.setAlignment(this.alignment, width, height);
    }

    public List<ITextLine> compileAndDraw(TextRenderer renderer, GuiContext context, boolean simulate) {
        renderer.setSimulate(simulate);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
        renderer.setSimulate(false);
        return this.cachedText;
    }

    public Object getHoveringElement(GuiContext context) {
        return getHoveringElement(context.getFont(), context.getMouseX(), context.getMouseY());
    }

    public Object getHoveringElement(Font fr, int x, int y) {
        if (this.cachedText == null) return null;

        for (ITextLine line : this.cachedText) {
            Object o = line.getHoveringElement(fr, x, y);
            if (o == null) continue;
            if (o == Boolean.FALSE) return null;
            return o;
        }
        return null;
    }
}
