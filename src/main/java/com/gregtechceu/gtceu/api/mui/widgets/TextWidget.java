package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class TextWidget<W extends TextWidget<W>> extends Widget<W> {

    @Getter
    private final IKey key;
    @Getter
    private Alignment alignment = Alignment.CenterLeft;
    @Getter
    private IntSupplier color = null;
    @Getter
    private Boolean shadow = null;
    @Getter
    private float scale = 1f;
    private int maxWidth = -1;

    private Component lastText = null;
    private Component textForDefaultSize = null;

    public TextWidget(IKey key) {
        this.key = key;
    }

    public TextWidget(String key) {
        this(IKey.str(key));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        Component text = checkString();
        WidgetTheme theme = getActiveWidgetTheme(widgetTheme, isHovering());
        renderer.setColor(this.color != null ? this.color.getAsInt() : theme.getTextColor());
        renderer.setAlignment(this.alignment, getArea().paddedWidth() + this.scale, getArea().paddedHeight());
        renderer.setShadow(this.shadow != null ? this.shadow : theme.isTextShadow());
        renderer.setPos(getArea().getPadding().left(), getArea().getPadding().top());
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(context.getGraphics(), text);
    }

    protected Component checkString() {
        Component text = this.key.getFormatted();
        if (this.lastText != null && !this.lastText.equals(text)) {
            onTextChanged(text);
            this.lastText = text;
        }
        this.lastText = text;
        return text;
    }

    protected void onTextChanged(Component newText) {
        // scheduling it would resize it on next frame, but we need it now
        WidgetTree.resizeInternal(this, false);
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left(), padding.top());
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(null, getComponentForDefaultSize());
        renderer.setSimulate(false);
        return renderer;
    }

    @Override
    public int getDefaultHeight() {
        float maxWidth;
        if (resizer().isWidthCalculated()) {
            maxWidth = getArea().width + this.scale;
        } else if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width + this.scale;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetHeight(renderer.getLastHeight());
    }

    @Override
    public int getDefaultWidth() {
        float maxWidth;
        if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetWidth(renderer.getLastWidth());
    }

    protected int getWidgetWidth(float actualTextWidth) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextWidth + padding.horizontal()));
    }

    protected int getWidgetHeight(float actualTextHeight) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextHeight + padding.vertical()));
    }

    @Override
    public boolean canHoverThrough() {
        return true;
    }

    protected Component getComponentForDefaultSize() {
        if (this.textForDefaultSize == null) {
            this.textForDefaultSize = this.key.get();
            this.lastText = this.textForDefaultSize;
        }
        return this.textForDefaultSize;
    }

    @Override
    public void postResize() {
        this.textForDefaultSize = null;
    }

    public W alignment(Alignment alignment) {
        this.alignment = alignment;
        return getThis();
    }

    public W color(int color) {
        return color(() -> color);
    }

    public W color(@Nullable IntSupplier color) {
        this.color = color;
        return getThis();
    }

    public W scale(float scale) {
        this.scale = scale;
        return getThis();
    }

    public W shadow(@Nullable Boolean shadow) {
        this.shadow = shadow;
        return getThis();
    }

    public W style(ChatFormatting formatting) {
        this.key.style(formatting);
        return getThis();
    }

    public W maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return getThis();
    }

    public Boolean isShadow() {
        return this.getShadow();
    }
}
