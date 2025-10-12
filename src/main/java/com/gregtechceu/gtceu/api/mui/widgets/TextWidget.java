package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
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

public class TextWidget extends Widget<TextWidget> {

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

    private Component lastText = Component.empty();
    private Component textForDefaultSize = Component.empty();

    public TextWidget(IKey key) {
        this.key = key;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        Component comp = this.key.getFormatted().copy();
        if (this.lastText != null && !this.lastText.equals(Component.empty()) && !this.lastText.equals(comp)) {
            WidgetTree.resizeInternal(this, false);
        }
        this.lastText = comp;
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getTextColor());
        renderer.setAlignment(this.alignment, getArea().w() + this.scale, getArea().h());
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.getTextShadow());
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(context.getGraphics(), this.key.getFormatted());
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left, padding.top);
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
        float maxWidth = getScreen().getScreenArea().width;
        if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
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

    protected Component getComponentForDefaultSize() {
        if (this.textForDefaultSize == null || this.textForDefaultSize.equals(Component.empty())) {
            this.textForDefaultSize = this.key.get();
            this.lastText = this.textForDefaultSize;
        }
        return this.textForDefaultSize;
    }

    @Override
    public void postResize() {
        this.textForDefaultSize = Component.empty();
    }

    public TextWidget alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public TextWidget color(int color) {
        return color(() -> color);
    }

    public TextWidget color(@Nullable IntSupplier color) {
        this.color = color;
        return this;
    }

    public TextWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextWidget shadow(@Nullable Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public TextWidget style(ChatFormatting formatting) {
        this.key.style(formatting);
        return this;
    }

    public Boolean isShadow() {
        return this.getShadow();
    }
}
