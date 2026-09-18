package brachy.modularui.api.widget;

import brachy.modularui.screen.viewport.ModularGuiContext;

/**
 * An interface for {@link IWidget}'s, that makes them focusable.
 * Usually used for text fields to receive keyboard and mouse input first, no matter if its hovered or not.
 */
public interface IFocusedWidget {

    /**
     * @return this widget is currently focused
     */
    boolean isFocused();

    /**
     * Called when this widget gets focused
     *
     * @param context gui context
     */
    void onFocus(ModularGuiContext context);

    /**
     * Called when the focus is removed from this widget
     *
     * @param context gui context
     */
    void onRemoveFocus(ModularGuiContext context);
}
