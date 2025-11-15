package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IHoverable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IRichTextBuilder;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.text.RichText;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RichTextWidget extends Widget<RichTextWidget> implements IRichTextBuilder<RichTextWidget>, Interactable {

    private final RichText text = new RichText();
    private Consumer<RichText> builder;
    private boolean dirty = false;
    private boolean autoUpdate = false;

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.autoUpdate || this.dirty) {
            if (this.builder != null) {
                this.text.clearText();
                this.builder.accept(this.text);
            }
            this.dirty = false;
        }
        this.text.drawAtZeroPadded(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (getHoveredElement(context) instanceof IHoverable hoverable) {
            hoverable.onHover();
            RichTooltip tooltip = hoverable.getTooltip();
            if (tooltip != null) {
                tooltip.draw(context);
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMousePressed(mouseX, mouseY, button);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public @NotNull Result onMouseTapped(double mouseX, double mouseY, int button) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseTapped(mouseX, mouseY, button);
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onKeyPressed(keyCode, scanCode, modifiers);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onKeyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onKeyTapped(keyCode, scanCode, modifiers);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseScrolled(mouseX, mouseY, delta);
        }
        return false;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (getHoveredElement() instanceof Interactable interactable) {
            interactable.onMouseDrag(mouseX, mouseY, button, dragX, dragY);
        }
    }

    /**
     * Returns the currently hovered element of this rich text or {@code null} if none is hovered.
     * Note that this method assumes, that the {@link com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack
     * IViewportStack}
     * is transformed to this widget.
     *
     * @return hovered element or null
     */
    @Nullable
    public Object getHoveredElement() {
        return getHoveredElement(getContext());
    }

    /**
     * Returns the currently hovered element of this rich text or {@code null} if none is hovered.
     * Note that this method assumes, that the {@link com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack
     * IViewportStack}
     * is transformed to this widget.
     *
     * @param context the viewport stack with transformation to this widget
     * @return hovered element or null
     */
    @Nullable
    public Object getHoveredElement(ModularGuiContext context) {
        if (!isHovering()) return null;
        return this.text.getHoveringElement(context);
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return text;
    }

    /**
     * Sets the auto update property. If auto update is true the text will be deleted each time it is drawn.
     * If {@link #builder} is not null, it will then be called.
     *
     * @param autoUpdate auto update
     * @return this
     */
    public RichTextWidget autoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return this;
    }

    /**
     * A builder which is called every time before drawing when {@link #dirty} is true.
     *
     * @param builder text builder
     * @return this
     */
    public RichTextWidget textBuilder(Consumer<RichText> builder) {
        this.builder = builder;
        markDirty();
        return this;
    }
}
