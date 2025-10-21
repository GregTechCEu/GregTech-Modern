package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.AbstractParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CategoryList extends AbstractParentWidget<IWidget, CategoryList> implements Interactable, ILayoutWidget {

    private final List<CategoryList> subCategories = new ArrayList<>();
    private boolean expanded = false;
    private int totalHeight = 0;
    private IDrawable expandedOverlay;
    private IDrawable collapsedOverlay;

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        if (this.expanded) {
            this.expandedOverlay.drawAtZero(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        } else {
            this.collapsedOverlay.drawAtZero(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.expandedOverlay == null) {
            if (getParent() instanceof CategoryList categoryList) {
                this.expandedOverlay = categoryList.expandedOverlay;
            } else if (getParent() instanceof Root root) {
                this.expandedOverlay = root.expandedOverlay;
            } else {
                this.expandedOverlay = IDrawable.EMPTY;
            }
        }
        if (this.collapsedOverlay == null) {
            if (getParent() instanceof CategoryList categoryList) {
                this.collapsedOverlay = categoryList.collapsedOverlay;
            } else if (getParent() instanceof Root root) {
                this.collapsedOverlay = root.collapsedOverlay;
            } else {
                this.collapsedOverlay = IDrawable.EMPTY;
            }
        }
    }

    @Override
    public void onChildAdd(IWidget child) {
        if (child instanceof CategoryList categoryList) {
            this.subCategories.add(categoryList);
        }
        child.setEnabled(this.expanded);
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            expanded(!this.expanded);
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    public void expanded(boolean expanded) {
        if (expanded == this.expanded) return;
        this.expanded = expanded;
        for (IWidget widget : getChildren()) {
            widget.setEnabled(expanded);
        }
        calculateHeightAndLayout(true);
    }

    public boolean calculateHeightAndLayout(boolean calculateParents) {
        if (this.expanded) {
            int y = getArea().height;
            for (IWidget widget : getChildren()) {
                widget.getArea().ry = y;
                widget.resizer().setYResized(true);
                if (!widget.resizer().isHeightCalculated()) return false;
                y += widget instanceof CategoryList categoryList && categoryList.expanded ?
                        categoryList.totalHeight : widget.getArea().height;
            }
            this.totalHeight = y;
        } else {
            this.totalHeight = getArea().height;
        }

        if (calculateParents) {
            if (getParent() instanceof CategoryList categoryList) {
                categoryList.calculateHeightAndLayout(true);
            } else if (getParent() instanceof Root root) {
                root.updateHeight();
            }
        }
        return true;
    }

    @Override
    public boolean layoutWidgets() {
        return calculateHeightAndLayout(false);
    }

    public CategoryList setCollapsedOverlay(IDrawable collapsedOverlay) {
        this.collapsedOverlay = collapsedOverlay;
        return this;
    }

    public CategoryList setExpandedOverlay(IDrawable expandedOverlay) {
        this.expandedOverlay = expandedOverlay;
        return this;
    }

    public static class Root extends ListWidget<IWidget, Root> {

        private final List<CategoryList> categories = new ArrayList<>();

        private IDrawable expandedOverlay = GTGuiTextures.MOVE_DOWN.asIcon().size(16, 8)
                .alignment(Alignment.CenterRight)
                .marginRight(4);
        private IDrawable collapsedOverlay = GTGuiTextures.MOVE_RIGHT.asIcon().size(8, 16)
                .alignment(Alignment.CenterRight).marginRight(8);

        @Override
        public void onChildAdd(IWidget child) {
            if (child instanceof CategoryList categoryList) {
                this.categories.add(categoryList);
            }
        }

        private void updateHeight() {
            layoutWidgets();
            WidgetTree.applyPos(this);
        }

        @Override
        public boolean layoutWidgets() {
            int y = 0;
            for (IWidget widget : getChildren()) {
                widget.getArea().ry = y;
                widget.resizer().setYResized(true);
                if (!widget.resizer().isHeightCalculated()) return false;
                y += widget instanceof CategoryList categoryList && categoryList.expanded ?
                        categoryList.totalHeight : widget.getArea().height;
            }
            getScrollArea().getScrollY().setScrollSize(y);
            return true;
        }

        public Root setCollapsedOverlay(IDrawable collapsedOverlay) {
            this.collapsedOverlay = collapsedOverlay;
            return this;
        }

        public Root setExpandedOverlay(IDrawable expandedOverlay) {
            this.expandedOverlay = expandedOverlay;
            return this;
        }
    }
}
