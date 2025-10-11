package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class ScrollingTextWidget extends TextWidget {

    private static final int pauseTime = 60;

    private TextRenderer.Line line = new TextRenderer.Line(FormattedCharSequence.EMPTY, 0);
    private long time = 0;
    private int scroll = 0;
    private boolean hovering = false;
    private int pauseTimer = 0;

    public ScrollingTextWidget(IKey key) {
        super(key);
        tooltipBuilder(tooltip -> {
            tooltip.showUpTimer(10);
            if (this.line.width() > getArea().width) {
                tooltip.addLine(key);
            }
        });
    }

    @Override
    public void onMouseStartHover() {
        this.hovering = true;
    }

    @Override
    public void onMouseEndHover() {
        this.hovering = false;
        this.scroll = 0;
        this.time = 0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.pauseTimer > 0) {
            if (++this.pauseTimer == pauseTime) {
                this.pauseTimer = this.scroll == 0 ? 0 : 1;
                this.scroll = 0;
            }
            return;
        }
        if (this.hovering && ++this.time % 2 == 0 && ++this.scroll == this.line.upperWidth() - getArea().width - 1) {
            this.pauseTimer = 1;
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        checkString();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setColor(getColor() != null ? getColor().getAsInt() : widgetTheme.getTextColor());
        renderer.setAlignment(getAlignment(), getArea().w() + 1, getArea().h());
        renderer.setShadow(isShadow() != null ? isShadow() : widgetTheme.getTextShadow());
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(getScale());
        renderer.setSimulate(false);
        if (this.hovering) {
            renderer.drawScrolling(context.getGraphics(), this.line, this.scroll, getArea(), context);
        } else {
            renderer.drawCut(context.getGraphics(), this.line);
        }
    }

    private void checkString() {
        var s = getKey().get().getVisualOrderText();
        if (!s.equals(this.line.text())) {
            TextRenderer.SHARED.setScale(getScale());
            this.line = TextRenderer.SHARED.line(s);
            this.scroll = 0;
            markTooltipDirty();
        }
    }

    @Override
    public ScrollingTextWidget alignment(Alignment alignment) {
        return (ScrollingTextWidget) super.alignment(alignment);
    }

    @Override
    public ScrollingTextWidget color(int color) {
        return color(() -> color);
    }

    @Override
    public ScrollingTextWidget color(@Nullable IntSupplier color) {
        return (ScrollingTextWidget) super.color(color);
    }

    @Override
    public ScrollingTextWidget scale(float scale) {
        return (ScrollingTextWidget) super.scale(scale);
    }

    @Override
    public ScrollingTextWidget shadow(@Nullable Boolean shadow) {
        return (ScrollingTextWidget) super.shadow(shadow);
    }

    @Override
    public ScrollingTextWidget widgetTheme(String widgetTheme) {
        return (ScrollingTextWidget) super.widgetTheme(widgetTheme);
    }
}
