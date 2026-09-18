package brachy.modularui.drawable.text;

import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.client.gui.Font;

import lombok.Getter;

import java.util.Objects;

/**
 * An icon which represents a {@link Text} object.
 * Note: This class assumes the string will be a single line!
 */
public class KeyIcon implements IIcon {

    @Getter
    private final Text key;
    private Font overrideFont;
    private final Box margin = new Box();
    private boolean expandWidth, expandHeight;

    public KeyIcon(Text key) {
        this.key = key;
    }

    public Font getFont() {
        return this.overrideFont != null ? this.overrideFont : MCHelper.getFont();
    }

    @Override
    public int getWidth() {
        return expandWidth ? 0 : getActualWidth();
    }

    @Override
    public int getHeight() {
        return expandHeight ? 0 : getActualHeight();
    }

    @Override
    public int getDefaultWidth() {
        return this.key.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight() {
        return this.key.getDefaultHeight();
    }

    public int getActualWidth() {
        return getFont().width(key.get()) + this.margin.horizontal();
    }

    public int getActualHeight() {
        return getFont().lineHeight + this.margin.vertical();
    }

    @Override
    public Box getMargin() {
        return null;
    }

    @Override
    public Text getWrappedDrawable() {
        return key;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int w = getActualWidth(), h = getActualHeight();
        x += (int) (width / 2f - w / 2f);
        y += (int) (height / 2f - h / 2f);
        this.key.draw(context, x, y, width, height, widgetTheme);
    }

    public KeyIcon expandWidth() {
        this.expandWidth = true;
        return this;
    }

    public KeyIcon expandHeight() {
        this.expandHeight = true;
        return this;
    }

    public KeyIcon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public KeyIcon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public KeyIcon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public KeyIcon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public KeyIcon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public KeyIcon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public KeyIcon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }

    public KeyIcon font(Font fr) {
        this.overrideFont = fr;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof KeyIcon that)) return false;

        return expandWidth == that.expandWidth && expandHeight == that.expandHeight &&
                key.equals(that.key) && Objects.equals(overrideFont, that.overrideFont) && margin.equals(that.margin);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + Objects.hashCode(overrideFont);
        result = 31 * result + margin.hashCode();
        result = 31 * result + Boolean.hashCode(expandWidth);
        result = 31 * result + Boolean.hashCode(expandHeight);
        return result;
    }

    @Override
    public String toString() {
        return "KeyIcon(" + this.key.get() + ")";
    }
}
