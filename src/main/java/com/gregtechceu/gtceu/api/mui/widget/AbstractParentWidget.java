package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.widgets.VoidWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;

/**
 * A widget which can hold any amount of children.
 *
 * @param <I> type of children (in most cases just {@link IWidget}). Use {@link VoidWidget} if no children should be
 *            added.
 * @param <W> type of this widget
 */
public class AbstractParentWidget<I extends IWidget, W extends AbstractParentWidget<I, W>> extends Widget<W> {

    private final List<I> children = new ArrayList<>();

    /**
     * A list of all children of this widget. The list is modifiable contrary to the annotation.
     * This just means that you shouldn't carelessly modify the list. Adding to the list also requires initialising the
     * new child.
     * Removing requires disposing the old child.
     *
     * @return a view of all children.
     */
    @SuppressWarnings("unchecked")
    @UnmodifiableView
    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return (List<IWidget>) this.children;
    }

    /**
     * A list of all children of this widget with the given children type {@link I}. The list is modifiable contrary to
     * the annotation.
     * This just means that you shouldn't carelessly modify the list. Adding to the list also requires initialising the
     * new child.
     * Removing requires disposing the old child.
     *
     * @return a view of all children.
     */
    @UnmodifiableView
    public List<I> getTypeChildren() {
        return children;
    }

    @Override
    public boolean canHover() {
        if (IDrawable.isVisible(getBackground()) ||
                IDrawable.isVisible(getHoverBackground()) ||
                IDrawable.isVisible(getHoverOverlay()) ||
                getTooltip() != null)
            return true;
        WidgetThemeEntry<?> widgetTheme = getWidgetTheme(getContext().getTheme());
        if (getBackground() == null && IDrawable.isVisible(widgetTheme.getTheme().getBackground())) return true;
        return getHoverBackground() == null && IDrawable.isVisible(widgetTheme.getHoverTheme().getBackground());
    }

    @Override
    public boolean canClickThrough() {
        return !canHover();
    }

    @Override
    public boolean canHoverThrough() {
        return !canHover();
    }

    protected boolean addChild(I child, int index) {
        if (child == null || child == this || getChildren().contains(child)) {
            return false;
        }
        if (child instanceof ModularPanel) {
            throw new IllegalArgumentException(
                    "ModularPanel should not be added as child widget; Use ModularScreen#openPanel instead");
        }
        if (!isChildValid(child)) {
            throw new IllegalArgumentException("Child '" + child + "' is not valid for parent '" + this + "'!");
        }
        if (index < 0) {
            index += getChildren().size() + 1;
        }
        this.children.add(index, child);
        if (isValid()) {
            child.initialise(this, true);
        }
        onChildAdd(child);
        return true;
    }

    protected boolean remove(I child) {
        if (this.children.remove(child)) {
            if (isValid()) {
                child.dispose();
            }
            onChildRemove(child);
            return true;
        }
        return false;
    }

    protected boolean remove(int index) {
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        I child = this.children.remove(index);
        if (this.isChildValid(child)) {
            child.dispose();
        }
        onChildRemove(child);
        return true;
    }

    protected boolean removeAll() {
        if (this.children.isEmpty()) return false;
        for (I i : this.children) {
            if (isValid()) i.dispose();
            onChildRemove(i);
        }
        this.children.clear();
        return true;
    }

    protected boolean isChildValid(I child) {
        return true;
    }

    protected void onChildAdd(I child) {}

    protected void onChildRemove(I child) {}
}
