package brachy.modularui.widgets;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.value.IIntValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;

import brachy.modularui.value.BoolValue;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * A button which cycles between 2 states by clicking on it. Background, overlay and tooltip can be supplied per state.
 *
 * @see CycleButtonWidget
 */
@Accessors(fluent = true, chain = true)
public class ToggleButton extends AbstractCycleButtonWidget<ToggleButton> {

    @Getter
    @Setter
    private boolean invertSelected = false;

    public ToggleButton() {
        stateCount(2);
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getToggleButtonTheme();
    }

    @Override
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        SelectableTheme selectableTheme = widgetTheme.expectType(SelectableTheme.class).getTheme(hover);
        return isValueSelected() ^ invertSelected() ? selectableTheme.getSelected() : selectableTheme;
    }

    public boolean isValueSelected() {
        return getState() == 1;
    }

    public ToggleButton value(IBoolValue<?> boolValue) {
        return super.value(boolValue);
    }

    public ToggleButton valueWrapped(IIntValue<?> intValue, int trueValue) {
        return value(new BoolValue.Dynamic(() -> intValue.getIntValue() == trueValue, v -> intValue.setIntValue(trueValue)));
    }

    public ToggleButton background(boolean selected, IDrawable drawable) {
        return disableThemeBackground(true).backgroundOverlay(selected, drawable);
    }

    public ToggleButton backgroundOverlay(boolean selected, IDrawable drawable) {
        getOrCreateState(selected ? 1 : 0).background = drawable;
        return this;
    }

    public ToggleButton hoverBackground(boolean selected, IDrawable drawable) {
        return disableHoverThemeBackground(true).hoverBackgroundOverlay(selected, drawable);
    }

    public ToggleButton hoverBackgroundOverlay(boolean selected, IDrawable drawable) {
        getOrCreateState(selected ? 1 : 0).hoverBackground = drawable;
        return this;
    }

    public ToggleButton overlay(boolean selected, IDrawable drawable) {
        getOrCreateState(selected ? 1 : 0).overlay = drawable;
        return getThis();
    }

    public ToggleButton hoverOverlay(boolean selected, IDrawable drawable) {
        getOrCreateState(selected ? 1 : 0).hoverOverlay = drawable;
        return getThis();
    }

    public ToggleButton addTooltip(boolean selected, String tooltip) {
        return super.addTooltip(selected ? 1 : 0, tooltip);
    }

    public ToggleButton addTooltip(boolean selected, IDrawable tooltip) {
        return super.addTooltip(selected ? 1 : 0, tooltip);
    }

    public ToggleButton tooltip(boolean selected, Consumer<RichTooltip> builder) {
        return super.tooltip(selected ? 1 : 0, builder);
    }

    public ToggleButton tooltipBuilder(boolean selected, Consumer<RichTooltip> builder) {
        return super.tooltipBuilder(selected ? 1 : 0, builder);
    }

    public ToggleButton child(boolean selected, IWidget widget) {
        return stateChild(selected ? 1 : 0, widget);
    }
}
