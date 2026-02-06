package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.widget.IPositioned;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;

import java.util.function.Consumer;

/**
 * A button that opens a context menu on click or hover.
 *
 * @param <W> type of this widget
 */
public class ContextMenuButton<W extends ContextMenuButton<W>> extends AbstractMenuButton<W> {

    /**
     * @param panelName the panel name that the menu may create
     */
    public ContextMenuButton(String panelName) {
        super(panelName);
        this.openOnHover = true;
    }

    @Override
    protected Menu<?> createMenu() {
        // menu is created by the user with the setter
        return null;
    }

    /**
     * Sets the menu widget. The menu will by default be relative to this button.
     * It is common to use {@link IPositioned#widthRel(float)} and {@link IPositioned#coverChildrenHeight()}.
     * The {@link #direction(Direction)} will handle the position of the menu, but can be customized if
     * {@link #openCustom()} is used.
     *
     * @param menu displayed menu
     * @return this
     */
    public W menu(Menu<?> menu) {
        setMenu(menu);
        return getThis();
    }

    /**
     * This is a shortcut that is meant to be used when a simple list of options should be displayed. It is recommended
     * to call
     * {@link com.gregtechceu.gtceu.api.mui.widgets.ListWidget#maxSize(int)} to limit the size. This is method is not
     * suited for any customization on the actual menu widget.
     *
     * @param builder list builder which is called exactly once
     * @return this
     */
    public W menuList(Consumer<ListWidget<IWidget, ?>> builder) {
        ListWidget<IWidget, ?> l = new ListWidget<>().widthRel(1f);
        builder.accept(l);
        return menu(new Menu<>()
                .widthRel(1f)
                .coverChildrenHeight()
                .child(l));
    }

    /**
     * Sets the general direction in which the menu should open. This is just a shortcut to {@link IPositioned} position
     * calls.
     * Use {@link #openCustom()} if none of the predefined options suit your needs. You can then position the menu
     * yourself.
     *
     * @param direction general direction to open the menu in
     * @return this
     */
    public W direction(Direction direction) {
        this.direction = direction;
        return getThis();
    }

    /**
     * Sets this button to require a click to open the menu. Hovering this button will not open it.
     *
     * @return this
     */
    public W requiresClick() {
        return openOnHover(false);
    }

    /**
     * Sets whether the menu should be opened when this button is hovered by the mouse.
     *
     * @param openOnHover true if the menu can be opened by hover
     * @return this
     */
    public W openOnHover(boolean openOnHover) {
        this.openOnHover = openOnHover;
        return getThis();
    }

    /**
     * Sets the menu to open in the "up" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public W openUp() {
        return direction(Direction.UP);
    }

    /**
     * Sets the menu to open in the "down" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public W openDown() {
        return direction(Direction.DOWN);
    }

    /**
     * Sets the menu to open in the "left and up" direction.
     *
     * @return this
     */
    public W openLeftUp() {
        return direction(Direction.LEFT_UP);
    }

    /**
     * Sets the menu to open in the "left and down" direction.
     *
     * @return this
     */
    public W openLeftDown() {
        return direction(Direction.LEFT_DOWN);
    }

    /**
     * Sets the menu to open in the "right and up" direction.
     *
     * @return this
     */
    public W openRightUp() {
        return direction(Direction.RIGHT_UP);
    }

    /**
     * Sets the menu to open in the "right and down" direction.
     *
     * @return this
     */
    public W openRightDown() {
        return direction(Direction.RIGHT_DOWN);
    }

    /**
     * Sets the menu to open in no specified direction. The position of the menu must be set manually, or it is left at
     * 0,0.
     *
     * @return this
     */
    public W openCustom() {
        return direction(Direction.UNDEFINED);
    }
}
