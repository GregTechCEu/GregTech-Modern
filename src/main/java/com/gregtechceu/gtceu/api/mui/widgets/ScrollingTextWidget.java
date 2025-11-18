package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.animation.Animator;
import com.gregtechceu.gtceu.api.mui.animation.IAnimator;
import com.gregtechceu.gtceu.api.mui.animation.SequentialAnimator;
import com.gregtechceu.gtceu.api.mui.animation.Wait;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Interpolation;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.network.chat.Component;

public class ScrollingTextWidget extends TextWidget<ScrollingTextWidget> {

    private TextRenderer.Line line;
    private float progress = 0;
    private IAnimator animator;
    private Animator forward;
    private Animator backward;

    private int speed = 15;

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
    public void dispose() {
        super.dispose();
        if (this.animator != null) {
            this.animator.stop(true);
        }
    }

    @Override
    public void onMouseStartHover() {
        super.onMouseStartHover();
        this.animator.resume(false);
    }

    @Override
    public void onMouseEndHover() {
        super.onMouseEndHover();
        this.animator.stop(true);
        this.animator.reset();
        this.progress = 0;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.animator == null) {
            animator(new Animator().curve(Interpolation.SINE_INOUT));
        }
        if (this.line == null) {
            updateLine(getKey().getFormatted());
        }
        checkString();
        WidgetTheme theme = getActiveWidgetTheme(widgetTheme, isHovering());
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setColor(getColor() != null ? getColor().getAsInt() : theme.getTextColor());
        renderer.setAlignment(getAlignment(), getArea().w(), getArea().h());
        renderer.setShadow(isShadow() != null ? isShadow() : theme.isTextShadow());
        renderer.setPos(getArea().getPadding().left(), getArea().getPadding().top());
        renderer.setScale(getScale());
        renderer.setSimulate(false);
        if (isHovering()) {
            renderer.drawScrolling(context.getGraphics(), this.line, this.progress, getArea(), context);
        } else {
            renderer.drawCut(context.getGraphics(), this.line);
        }
    }

    @Override
    protected void onTextChanged(Component newText) {
        super.onTextChanged(newText);
        updateLine(newText);
        markTooltipDirty();
    }

    protected void updateLine(Component newText) {
        TextRenderer.SHARED.setScale(getScale());
        this.line = TextRenderer.SHARED.line(newText.getVisualOrderText());
        this.animator.stop(true);
        this.animator.reset();
        this.forward.duration(this.line.upperWidth() * this.speed);
        this.backward.duration(this.forward.getDuration() * 3 / 4);
    }

    /**
     * Sets the scroll speed when hovered. This sets not the speed directly, but the duration per pixel in milliseconds.
     * So if the text is
     * 100 lines long and the "speed" is set to 10, then the whole animation is 1000 milliseconds long.
     *
     * @param speed duration per pixel in milliseconds (default is 15)
     * @return this
     */
    public ScrollingTextWidget scrollSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public ScrollingTextWidget animator(Animator animator) {
        this.forward = animator.onUpdate((double val) -> this.progress = (float) (val));
        this.backward = animator.copy(true);
        this.animator = new SequentialAnimator(this.forward, new Wait(500), this.backward, new Wait(1000))
                .repeatsOnFinish(20);
        return this;
    }
}
