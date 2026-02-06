package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.StandardResizer;

import net.minecraft.ChatFormatting;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is the base class for a button that can open a floating widget by clicking or hovering the button. In ModularUI
 * this is used for
 * context menus and dropdown menus. When the menu is opened a new panel is created and the {@link Menu} widget is
 * added. If that menu
 * contains another one of these menu buttons, it will be added to that panel.
 *
 * @param <W> type of this widget
 */
public abstract class AbstractMenuButton<W extends AbstractMenuButton<W>> extends Widget<W>
                                        implements IMenuPart, Interactable {

    /**
     * The general direction where the menu will be opened. This is just a shortcut to standard resizer calls.
     * If this is null you can customize the position yourself.
     */
    protected Direction direction = Direction.DOWN;
    /**
     * If this is true, the menu can be opened when the mouse hovers this button. The menu will automatically close,
     * when the button AND
     * all widgets in the menus tree are no longer hovered.
     */
    protected boolean openOnHover = true;

    /**
     * The current menu widget. The menu will be created with {@link #createMenu()} if the menu is null when it's
     * needed. If the method
     * also returns a null value, a default menu is created. The menu can be set at any time with
     * {@link #setMenu(Menu)}.
     */
    private Menu<?> menu;
    /**
     * @return true if the menu is currently open (soft or hard)
     */
    @Getter
    private boolean open;
    private boolean softOpen; // state, soft means opened by hovering
    private IPanelHandler panelHandler;
    private final String panelName;

    /**
     * @param panelName the name for the panel that may be created when opening the menu
     */
    public AbstractMenuButton(String panelName) {
        this.panelName = Objects.requireNonNull(panelName);
        name(panelName);
    }

    /**
     * @return true if the menu is currently soft open (opened by hovering)
     */
    protected boolean isSoftOpen() {
        return softOpen;
    }

    protected void toggleMenu(boolean soft) {
        if (this.open) {
            if (this.softOpen) {
                if (soft) {
                    closeMenu(true);
                } else {
                    this.softOpen = false;
                }
            } else if (!soft) {
                if (this.openOnHover) {
                    this.softOpen = true;
                } else {
                    closeMenu(false);
                }
            }
        } else {
            openMenu(soft);
        }
    }

    protected void openMenu(boolean soft) {
        if (this.open) {
            if (this.softOpen && !soft) {
                this.softOpen = false;
            }
            return;
        }
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.openSubMenu(getMenu());
        } else {
            getPanelHandler().openPanel();
        }
        this.open = true;
        this.softOpen = soft;
    }

    protected void closeMenu(boolean soft) {
        if (!this.open || (!this.softOpen && soft)) return;
        if (getPanel() instanceof MenuPanel menuPanel) {
            menuPanel.remove(getMenu());
        } else {
            getPanelHandler().closePanel();
        }
        this.open = false;
        this.softOpen = false;
    }

    protected Menu<?> getMenu() {
        if (this.menu == null) {
            this.menu = createMenu();
            if (this.menu == null) {
                this.menu = new Menu<>()
                        .child(IKey.str("No Menu supplied")
                                .style(ChatFormatting.RED)
                                .asWidget()
                                .center())
                        .widthRel(1f)
                        .height(16);
                if (this.direction == null) Direction.DOWN.positioner.accept(this.menu.resizer());
            }
        }
        if (!this.menu.resizer().hasParentOverride()) {
            this.menu.resizer().relative(this);
        }
        if (this.direction != null) {
            this.direction.positioner.accept(this.menu.resizer());
        }
        this.menu.setMenuSource(this);
        return this.menu;
    }

    protected void setMenu(Menu<?> menu) {
        this.menu = menu;
    }

    protected abstract Menu<?> createMenu();

    private IPanelHandler getPanelHandler() {
        if (this.panelHandler == null) {
            this.panelHandler = IPanelHandler.simple(getPanel(),
                    (parentPanel, player) -> new MenuPanel(this.panelName, getMenu()), true);
        }
        return this.panelHandler;
    }

    @Override
    public @NotNull Result onMousePressed(double x, double y, int mouseButton) {
        if (!this.open) {
            forEachSiblingMenuButton(w -> {
                w.closeMenu(false);
                return true;
            });
        }
        toggleMenu(false);
        return Result.SUCCESS;
    }

    @Override
    public void onMouseEnterArea() {
        super.onMouseEnterArea();
        if (this.openOnHover && forEachSiblingMenuButton(mb -> !mb.open || mb.softOpen)) {
            openMenu(true);
        }
    }

    protected boolean forEachSiblingMenuButton(Predicate<AbstractMenuButton<?>> test) {
        Menu<?> menuParent = WidgetTree.findParent(this, Menu.class);
        if (menuParent != null) {
            return WidgetTree.foreachChild(menuParent,
                    w -> !(w instanceof AbstractMenuButton<?> mb) || mb == this || test.test(mb), false);
        }
        return true;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose();
    }

    protected void checkClose() {
        if (this.openOnHover && !isSelfOrChildHovered()) {
            closeMenu(true);
            Menu<?> menuParent = WidgetTree.findParent(this, Menu.class);
            if (menuParent != null) {
                menuParent.checkClose();
            }
        }
    }

    @Override
    public boolean isSelfOrChildHovered() {
        if (isBelowMouse()) return true;
        if (!isOpen() || this.menu == null) return false;
        return this.menu.isSelfOrChildHovered();
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    public enum Direction {

        UP(resizer -> resizer.bottomRel(1f)),
        DOWN(resizer -> resizer.topRel(1f)),
        LEFT_UP(resizer -> resizer.rightRel(1f).bottom(0)),
        LEFT_DOWN(resizer -> resizer.rightRel(1f).top(0)),
        RIGHT_UP(resizer -> resizer.leftRel(1f).bottom(0)),
        RIGHT_DOWN(resizer -> resizer.leftRel(1f).top(0)),
        UNDEFINED(resizer -> {});

        private final Consumer<StandardResizer> positioner;

        Direction(Consumer<StandardResizer> positioner) {
            this.positioner = positioner;
        }
    }
}
