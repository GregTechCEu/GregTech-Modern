package brachy.modularui.drawable.text;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IHoverable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

import lombok.Getter;

import java.util.List;

public class ComposedLine implements ITextLine {

    private final List<Object> elements;
    @Getter
    private final int width;
    private final int height;

    private float lastX, lastY;

    public ComposedLine(List<Object> elements, int width, int height) {
        this.elements = elements;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getHeight(Font font) {
        return height == font.lineHeight ? height : height + 1;
    }

    @Override
    public void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow,
                     int availableWidth, int availableHeight) {
        this.lastX = x;
        this.lastY = y;
        for (Object o : this.elements) {
            if (o instanceof FormattedCharSequence s) {
                float drawY = getHeight(font) / 2f - font.lineHeight / 2f;
                context.getGraphics().drawString(font, s, x, y + drawY, color, shadow);
                x += font.width(s);
            } else if (o instanceof IIcon icon) {
                float drawY = getHeight(font) / 2f - icon.getHeight() / 2f;
                int w = icon.getWidth() > 0 ? icon.getWidth() : availableWidth;
                icon.draw(context, (int) x, (int) (y + drawY), w, icon.getHeight(),
                        IThemeApi.get().getDefaultTheme().getFallback().theme());
                if (icon instanceof IHoverable hoverable) {
                    hoverable.setRenderedAt((int) x, (int) (y + drawY));
                }
                x += w;
            } else if (o != null) {
                throw new IllegalArgumentException("ComposedLine objects must either be a FormattedCharSequence or an IIcon, but found a " + o.getClass().getSimpleName() + "!");
            }
        }
    }

    @Override
    public Object getHoveringElement(Font font, int x, int y) {
        int h0 = getHeight(font);
        if (y < lastY || y > lastY + h0) return null; // is not hovering vertically
        if (x < lastX || x > lastX + getWidth()) return Boolean.FALSE; // is not hovering horizontally
        float x0 = x - this.lastX; // origin to 0
        float x1 = 0;
        float y0 = y - this.lastY; // origin to 0
        for (Object o : this.elements) {
            float w, h;
            if (o instanceof FormattedCharSequence s) {
                w = font.width(s);
                h = font.lineHeight;
            } else if (o instanceof IIcon icon) {
                w = icon.getWidth();
                h = icon.getWidth();
            } else continue;
            if (x0 > x1 && x0 < x1 + w) {
                // is inside horizontally
                if (h < h0) {
                    // is smaller than line height
                    int lower = (int) (h0 / 2f - h / 2);
                    int upper = (int) (h0 / 2f + h / 2 - 1f);
                    if (y0 < lower || y0 > upper) return Boolean.FALSE; // is outside vertically
                }
                return o; // found hovering
            }
            x1 += w; // move to next element
        }
        return null;
    }
}
