package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.ISyncOrValue;
import com.gregtechceu.gtceu.api.mui.base.value.IValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.MutableSingletonList;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A button which displays a list of options when clicked. When an option is clicked, the menu closes and the button
 * displays that selected
 * option. To use it, supply the appropriate value type you want to use to the constructor. This is used to validate
 * synced values.
 * Add options that can be selected with {@link #option(Object)}, {@link #options(Iterable)} or
 * {@link #options(Object[])}.
 * Finally set a function that converts an option to a widget using {@link #optionToWidget(ToWidget)}. When a value is
 * selected, it will
 * sync to any set value handler.
 *
 * @param <T> type of the values used
 * @param <W> type of this widget
 */
public class DropdownWidget<T, W extends DropdownWidget<T, W>> extends AbstractMenuButton<W> {

    private final Class<T> valueType;
    private final MutableSingletonList<IWidget> selected = new MutableSingletonList<>();
    private final List<T> values = new ArrayList<>();
    private IValue<T> value;
    private int maxListSize = 100;
    private ToWidget<T> toWidget;

    public DropdownWidget(String panelName, Class<T> valueType) {
        super(panelName);
        this.valueType = valueType;
        this.openOnHover = false;
    }

    @Override
    public void onInit() {
        super.onInit();
        setValue(this.value.getValue(), false);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return selected;
    }

    protected IWidget valueToWidget(T v, boolean forSelectedDisplay) {
        if (this.toWidget != null) {
            return this.toWidget.apply(v, forSelectedDisplay);
        }
        return IKey.str(String.valueOf(v)).asWidget();
    }

    protected void setValue(T value, boolean updateValue) {
        if (this.selected.hasValue()) {
            this.selected.get().dispose();
        }
        if (updateValue && this.value != null) this.value.setValue(value);
        this.selected.set(valueToWidget(value, true));
        if (isValid()) {
            this.selected.get().initialise(this, true);
            scheduleResize();
        }
    }

    @Override
    protected Menu<?> createMenu() {
        return new Menu<>()
                .widthRel(1f)
                .coverChildrenHeight()
                .child(new ListWidget<>()
                        .widthRel(1f)
                        .maxSize(this.maxListSize)
                        .children(this.values, v -> new ButtonWidget<>()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .child(valueToWidget(v, false))
                                .onMousePressed((x, y, b) -> {
                                    setValue(v, true);
                                    closeMenu(false);
                                    return true;
                                })));
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isValueOfType(this.valueType);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castValueNullable(this.valueType);
    }

    /**
     * Deletes the current cached menu. This can be used if the list of options changes while the menu is open.
     * If the menu is currently open it won't be affected.
     */
    public void deleteMenu() {
        setMenu(null);
    }

    /**
     * Sets a value handler that handles the selected value.
     *
     * @param value value handler
     * @return this
     */
    public W value(IValue<T> value) {
        setSyncOrValue(value);
        return getThis();
    }

    /**
     * Adds an option that can be selected.
     *
     * @param option selectable option
     * @return this
     */
    public W option(T option) {
        this.values.add(option);
        return getThis();
    }

    /**
     * Adds an iterable of selectable options.
     *
     * @param options selectable options
     * @return this
     */
    public W options(Iterable<T> options) {
        for (T t : options) this.values.add(t);
        return getThis();
    }

    /**
     * Adds an array of selectable options.
     *
     * @param options selectable options
     * @return this
     */
    public W options(T... options) {
        this.values.addAll(Arrays.asList(options));
        return getThis();
    }

    /**
     * Clears all currently set selectable options.
     *
     * @return this
     */
    public W clearOptions() {
        this.values.clear();
        return getThis();
    }

    /**
     * Sets a function which converts options into displayable widgets. The function is called once for each option when
     * the menu is created
     * and each time when a value is selected. The function itself has a value and boolean parameter. The value is the
     * option which is
     * displayed and the boolean is true if the widget is created for the selected display. There is no limit to what
     * combination of widgets
     * an option can be. Usually it is a text or a row with an icon and text.
     * If this function is not a set {@link String#valueOf(Object)} is used to display text.
     *
     * @param toWidget function to create a display widget from an option
     * @return this
     * @see ToWidget
     */
    public W optionToWidget(ToWidget<T> toWidget) {
        this.toWidget = toWidget;
        return getThis();
    }

    /**
     * Sets the maximum size of the list widget in pixel. By default, it is set to 100.
     *
     * @param maxListSize maximum list size in pixel
     * @return this
     */
    public W maxVerticalMenuSize(int maxListSize) {
        this.maxListSize = maxListSize;
        return getThis();
    }

    /**
     * Sets the menu to open in the "up" direction.
     *
     * @return this
     */
    public W directionUp() {
        this.direction = Direction.UP;
        return getThis();
    }

    /**
     * Sets the menu to open in the "down" direction.
     *
     * @return this
     */
    public W directionDown() {
        this.direction = Direction.DOWN;
        return getThis();
    }

    public interface ToWidget<T> {

        /**
         * A function to convert a value into a display widget.
         *
         * @param value              value to display
         * @param forSelectedDisplay if the widget is used on the button when the menu is closed
         * @return this
         */
        IWidget apply(T value, boolean forSelectedDisplay);
    }
}
