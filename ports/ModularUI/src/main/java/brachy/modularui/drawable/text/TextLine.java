package brachy.modularui.drawable.text;

import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

import lombok.Getter;

public class TextLine implements ITextLine {

    private final FormattedCharSequence text;
    @Getter
    private final int width;

    private float lastX, lastY;

    public TextLine(FormattedCharSequence text, int width) {
        this.text = text;
        this.width = width;
    }

    @Override
    public int getHeight(Font font) {
        return font.lineHeight + 1;
    }

    @Override
    public void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow,
                     int availableWidth, int availableHeight) {
        context.getGraphics().drawString(font, this.text, x, y, color, shadow);
        this.lastX = x;
        this.lastY = y;
    }

    @Override
    public Object getHoveringElement(Font font, int x, int y) {
        if (y < lastY || y > lastY + getHeight(font)) return null;
        // not hovering, but we know that nothing else is hovered either
        if (x < lastX || x > lastX + getWidth()) return Boolean.FALSE;
        return this.text;
    }
}
