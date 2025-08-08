package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PopupMenu<W extends PopupMenu<W>> extends Widget<W> {

    private final MenuWrapper menu;
    @Getter
    private final @NotNull List<IWidget> children;

    public PopupMenu(IWidget child) {
        this.menu = new MenuWrapper(child);
        child.flex().relative(this.getArea());
        this.menu.setEnabled(false);
        this.children = Collections.singletonList(this.menu);
    }

    @Override
    public void onMouseStartHover() {
        this.menu.setEnabled(true);
        this.menu.mightClose = false;
    }

    @Override
    public void onMouseEndHover() {
        this.menu.mightClose = true;
    }

    public static class MenuWrapper extends Widget<MenuWrapper> {

        private final IWidget child;
        @Getter
        private final @NotNull List<IWidget> children;
        @Setter
        private boolean mightClose = false;

        private MenuWrapper(IWidget child) {
            this.child = child;
            this.children = Collections.singletonList(child);
            flex().coverChildren().cancelMovementX().cancelMovementY();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (this.mightClose && !isBelowMouse()) {
                setEnabled(false);
            }
        }
    }
}
