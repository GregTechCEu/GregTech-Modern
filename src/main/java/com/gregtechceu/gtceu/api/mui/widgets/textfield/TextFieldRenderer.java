package com.gregtechceu.gtceu.api.mui.widgets.textfield;

import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.FontRenderHelper;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Point;
import com.gregtechceu.gtceu.api.mui.utils.PointF;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collections;
import java.util.List;

public class TextFieldRenderer extends TextRenderer {

    protected final TextFieldHandler handler;
    @Setter
    protected int markedColor = 0x2F72A8;
    @Setter
    protected int cursorColor = 0xFFFFFFFF;
    protected boolean renderCursor = false;

    public TextFieldRenderer(TextFieldHandler handler) {
        this.handler = handler;
    }

    public void toggleCursor() {
        this.renderCursor = !this.renderCursor;
    }

    public void setCursor(boolean active) {
        this.renderCursor = active;
    }

    @Override
    protected void drawMeasuredLines(GuiGraphics graphics, List<Line> measuredLines) {
        drawMarked(graphics, measuredLines);
        super.drawMeasuredLines(graphics, measuredLines);
        // draw cursor
        if (this.renderCursor) {
            Point main = this.handler.getMainCursor();
            PointF start = getPosOf(measuredLines, main);
            if (this.handler.getText().get(main.y).isEmpty()) {
                start.x += 0.7f;
            }
            drawCursor(graphics, start.x, start.y);
        }
    }

    @Override
    public List<FormattedCharSequence> wrapLine(Component line) {
        return Collections.singletonList(line.getVisualOrderText());
    }

    protected void drawMarked(GuiGraphics graphics, List<Line> measuredLines) {
        if (!this.simulate && this.handler.hasTextMarked()) {
            PointF start = getPosOf(measuredLines, this.handler.getStartCursor());
            // render Marked
            PointF end = getPosOf(measuredLines, this.handler.getEndCursor());

            if (start.y == end.y) {
                drawMarked(graphics, start.y, start.x, end.x);
            } else {
                int min = this.handler.getStartCursor().y;
                int max = this.handler.getEndCursor().y;
                Line line = measuredLines.get(min);
                int startX = getStartX(line.width());
                drawMarked(graphics, start.y, start.x, startX + line.width());
                start.y += getFontHeight();
                if (max - min > 1) {
                    for (int i = min + 1; i < max; i++) {
                        line = measuredLines.get(i);
                        startX = getStartX(line.width());
                        drawMarked(graphics, start.y, startX, startX + line.width());
                        start.y += getFontHeight();
                    }
                }
                line = measuredLines.get(max);
                startX = getStartX(line.width());
                drawMarked(graphics, start.y, startX, end.x);
            }
        }
    }

    public Point getCursorPos(List<String> lines, int x, int y) {
        if (lines.isEmpty()) {
            return new Point();
        }
        List<Line> measuredLines = measureStringLines(lines);
        y -= getStartY(measuredLines.size());
        int lineIndex = (int) (y / (getFontHeight()));
        if (lineIndex < 0) return new Point();
        if (lineIndex >= measuredLines.size()) {
            return new Point(FontRenderHelper.length(measuredLines.get(measuredLines.size() - 1).text()),
                    measuredLines.size() - 1);
        }
        Line line = measuredLines.get(lineIndex);
        x -= getStartX(line.width());
        if (line.width() <= 0) return new Point(0, lineIndex);
        if (line.width() < x) return new Point(FontRenderHelper.length(line.text()), lineIndex);

        final float fx = x;
        final MutableFloat currentX = new MutableFloat();
        final MutableInt xIndex = new MutableInt();
        line.text().accept((positionInCurrentSequence, style, codePoint) -> {
            float charWidth = getFont().width(FormattedCharSequence.codepoint(codePoint, style));
            currentX.add(charWidth);
            if (currentX.floatValue() >= fx) {
                // dist with current letter < dist without current letter -> next letter pos
                if (Math.abs(currentX.floatValue() - fx) < Math.abs(currentX.floatValue() - charWidth - fx)) {
                    xIndex.increment();
                }
                return false;
            }
            xIndex.increment();
            return true;
        });
        return new Point(xIndex.intValue(), lineIndex);
    }

    public PointF getPosOf(List<Line> measuredLines, Point cursorPos) {
        if (measuredLines.isEmpty()) {
            return new PointF(getStartX(0), getStartYOfLines(1));
        }
        Line line = measuredLines.get(cursorPos.y);
        float width = getFont().getSplitter().stringWidth(FontRenderHelper.substring(line.text(), 0, cursorPos.x + 1));
        return new PointF(getStartX(line.width()) + width * this.scale,
                getStartYOfLines(measuredLines.size()) + cursorPos.y * getFontHeight());
    }

    @OnlyIn(Dist.CLIENT)
    public void drawMarked(GuiGraphics graphics, float y0, float x0, float x1) {
        y0 -= 1;
        RenderSystem.enableBlend();
        GuiDraw.drawRect(graphics, x0, y0, x1 - x0, getFontHeight(), this.markedColor);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawCursor(GuiGraphics graphics, float x0, float y0) {
        x0 = (x0 - 0.8f) / this.scale;
        y0 = (y0 - 1) / this.scale;

        graphics.pose().pushPose();
        graphics.pose().scale(this.scale, this.scale, 1);
        RenderSystem.enableBlend();
        GuiDraw.drawRect(graphics, x0, y0, 0.6f, 9, this.cursorColor);
        graphics.pose().popPose();
    }
}
