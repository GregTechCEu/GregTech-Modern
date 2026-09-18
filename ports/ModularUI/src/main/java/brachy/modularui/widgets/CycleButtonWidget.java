package brachy.modularui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.value.IEnumValue;
import brachy.modularui.api.value.IIntValue;
import brachy.modularui.screen.RichTooltip;

import java.util.function.Consumer;

/**
 * A button which cycles between multiple states by clicking on it. Background, overlay and tooltip can be supplied perstate.
 * <p>
 * Note that you need to set the amount of states before setting any state backgrounds etc. The state count is
 * automatically set, if the passed {@link IIntValue} is a {@link IEnumValue IEnumValue} or a {@link IBoolValue IBoolValue}.
 * </p>
 *
 * @see ToggleButton
 */
public class CycleButtonWidget extends AbstractCycleButtonWidget<CycleButtonWidget> {

    @Override
    public CycleButtonWidget value(IIntValue<?> value) {
        return super.value(value);
    }

    public CycleButtonWidget stateBackground(int state, IDrawable drawable) {
        return disableThemeBackground(true).stateBackgroundOverlay(state, drawable);
    }

    public CycleButtonWidget stateBackgroundOverlay(int state, IDrawable drawable) {
        getOrCreateState(state).background = drawable;
        return this;
    }

    public CycleButtonWidget stateHoverBackground(int state, IDrawable drawable) {
        return disableHoverThemeBackground(true).stateHoverBackgroundOverlay(state, drawable);
    }

    public CycleButtonWidget stateHoverBackgroundOverlay(int state, IDrawable drawable) {
        getOrCreateState(state).hoverBackground = drawable;
        return this;
    }

    public CycleButtonWidget stateOverlay(int state, IDrawable drawable) {
        getOrCreateState(state).overlay = drawable;
        return getThis();
    }

    public CycleButtonWidget stateHoverOverlay(int state, IDrawable drawable) {
        getOrCreateState(state).hoverOverlay = drawable;
        return getThis();
    }

    public CycleButtonWidget stateBackground(boolean state, IDrawable drawable) {
        return stateBackground(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateBackgroundOverlay(boolean state, IDrawable drawable) {
        return stateBackgroundOverlay(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateHoverBackground(boolean state, IDrawable drawable) {
        return stateHoverBackground(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateHoverBackgroundOverlay(boolean state, IDrawable drawable) {
        return stateHoverBackgroundOverlay(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateOverlay(boolean state, IDrawable drawable) {
        return stateOverlay(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateHoverOverlay(boolean state, IDrawable drawable) {
        return stateHoverOverlay(state ? 1 : 0, drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateBackground(T state, IDrawable drawable) {
        return stateBackground(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateBackgroundOverlay(T state, IDrawable drawable) {
        return stateBackgroundOverlay(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverBackground(T state, IDrawable drawable) {
        return stateHoverBackground(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverBackgroundOverlay(T state, IDrawable drawable) {
        return stateHoverBackgroundOverlay(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateOverlay(T state, IDrawable drawable) {
        return stateOverlay(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverOverlay(T state, IDrawable drawable) {
        return stateHoverOverlay(state.ordinal(), drawable);
    }

    @Override
    public CycleButtonWidget addTooltip(int state, String tooltip) {
        return super.addTooltip(state, tooltip);
    }

    @Override
    public CycleButtonWidget addTooltip(int state, IDrawable tooltip) {
        return super.addTooltip(state, tooltip);
    }

    public CycleButtonWidget length(int length) {
        return stateCount(length);
    }

    @Override
    public CycleButtonWidget stateCount(int stateCount) {
        return super.stateCount(stateCount);
    }

    @Override
    public CycleButtonWidget tooltip(int index, Consumer<RichTooltip> builder) {
        return super.tooltip(index, builder);
    }

    @Override
    public CycleButtonWidget tooltipBuilder(int index, Consumer<RichTooltip> builder) {
        return super.tooltipBuilder(index, builder);
    }
}
