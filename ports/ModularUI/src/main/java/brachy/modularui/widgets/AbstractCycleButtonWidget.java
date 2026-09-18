package brachy.modularui.widgets;

import brachy.modularui.ModularUI;
import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.value.IEnumValue;
import brachy.modularui.api.value.IIntValue;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.IntValue;
import brachy.modularui.widget.SingleChildWidget;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AbstractCycleButtonWidget<W extends AbstractCycleButtonWidget<W>> extends SingleChildWidget<W> implements Interactable {

    private static final State[] EMPTY = new State[0];

    @Getter private int stateCount = 0;
    private boolean explicitStateCount = false;
    private boolean hasCount = false;
    @Getter private IIntValue<?> intValue;
    private int lastValue = -1;
    private State[] states = EMPTY;
    protected IWidget fallbackChild = null;

    @Override
    public void onInit() {
        if (this.intValue == null) {
            this.intValue = new IntValue(0);
        }
        updateChild(getState());
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IIntValue.class);
    }

    protected void updateStateCount(int count, boolean explicit) {
        if (count < 1) return;
        if (explicit) {
            setStateCount(count);
            this.explicitStateCount = true;
        } else if (!this.explicitStateCount && count > this.stateCount) {
            setStateCount(count);
        }
    }

    private void setStateCount(int stateCount) {
        this.hasCount = true;
        if (this.stateCount == stateCount) return;
        this.stateCount = stateCount;

        while (this.states.length < stateCount) {
            this.states = ArrayUtils.add(this.states, new State());
        }
        if (this.states.length > stateCount) {
            for (int i = stateCount; i < this.states.length; i++) {
                this.states[i].reset();
            }
        }
    }

    public State getOrCreateState(int index) {
        updateStateCount(index + 1, false);
        if (this.stateCount <= index) {
            if (this.explicitStateCount) {
                throw new IllegalArgumentException("Could not get state %d of widget %s. The state count was explicitly set to %d.".formatted(index, this, this.stateCount));
            }
            throw new IllegalStateException("For some reason less states are available then expected.");
        }
        return this.states[index];
    }

    protected void forEachState(Consumer<State> consumer) {
        for (int i = 0; i < this.stateCount; i++) {
            consumer.accept(this.states[i]);
        }
    }

    protected void forEachTooltip(Consumer<RichTooltip> consumer) {
        forEachState(s -> consumer.accept(s.tooltip(this)));
    }

    protected IDrawable getStateBackground(int index) {
        return index < this.stateCount ? this.states[index].background : null;
    }

    protected IDrawable getStateOverlay(int index) {
        return index < this.stateCount ? this.states[index].overlay : null;
    }

    protected IDrawable getStateHoverBackground(int index) {
        return index < this.stateCount ? this.states[index].hoverBackground : null;
    }

    protected IDrawable getStateHoverOverlay(int index) {
        return index < this.stateCount ? this.states[index].hoverOverlay : null;
    }

    protected RichTooltip getStateTooltip(int index) {
        return index < this.stateCount ? this.states[index].tooltip(this) : null;
    }

    protected boolean hasStateTooltip(int index) {
        return index < this.stateCount && this.states[index].hasTooltip();
    }

    protected IWidget getStateChild(int index) {
        return index < this.stateCount ? this.states[index].child : null;
    }

    protected void expectCount() {
        if (!this.hasCount) {
            ModularUI.LOGGER.error("State count for widget {} is required, but has not been set yet!", this);
        }
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.intValue = syncOrValue.castNullable(IIntValue.class);
        if (syncOrValue instanceof IEnumValue<?> enumValue) {
            updateStateCount(enumValue.getEnumClass().getEnumConstants().length, true);
        } else if (syncOrValue instanceof IBoolValue) {
            updateStateCount(2, true);
        }
    }

    protected int getState() {
        int val = this.intValue.getIntValue();
        if (val != this.lastValue) {
            setState(val, false);
        }
        return val;
    }

    public void next() {
        int state = (getState() + 1) % this.stateCount;

        setState(state, true);
    }

    public void prev() {
        int state = getState();
        if (--state == -1) {
            state = this.stateCount - 1;
        }
        setState(state, true);
    }

    public void setState(int state, boolean setSource) {
        if (state < 0 || state >= this.stateCount) {
            throw new IndexOutOfBoundsException("CycleButton state out of bounds");
        }
        updateChild(state);
        if (setSource) {
            this.intValue.setIntValue(state);
        }
        this.lastValue = state;
        markTooltipDirty();
    }

    private void updateChild(int state) {
        IWidget child = getStateChild(state);
        if (child != null) {
            child(child);
        } else if (getChild() != this.fallbackChild) {
            child(this.fallbackChild);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        switch (button) {
            case 0:
                next();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            case 1:
                prev();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public IDrawable getCurrentBackground(WidgetThemeEntry<?> widgetTheme) {
        // make sure texture is up-to-date
        int state = getState();
        if (isHovering()) {
            var hbg = getStateHoverBackground(state);
            if (hbg != null && hbg != IDrawable.NONE) {
                return hbg;
            }
        }
        var bg = getStateBackground(state);
        return bg != null ? bg : super.getCurrentBackground(widgetTheme);
    }

    @Override
    public IDrawable getCurrentOverlay(WidgetThemeEntry<?> widgetTheme) {
        int state = getState();
        if (isHovering()) {
            var hbg = getStateHoverOverlay(state);
            if (hbg != null && hbg != IDrawable.NONE) {
                return hbg;
            }
        }
        var bg = getStateOverlay(state);
        return bg != null ? bg : super.getCurrentOverlay(widgetTheme);
    }

    @Override
    public boolean hasTooltip() {
        return super.hasTooltip() || hasStateTooltip(getState());
    }

    @Override
    public void markTooltipDirty() {
        super.markTooltipDirty();
        forEachState(state -> {
            if (state.tooltip != null) {
                state.tooltip.markDirty();
            }
        });
        getState();
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        RichTooltip tooltip = super.getTooltip();
        if (tooltip == null || tooltip.isEmpty()) {
            return getStateTooltip(getState());
        }
        return tooltip;
    }

    @Override
    public W disableHoverBackground() {
        expectCount();
        forEachState(state -> state.hoverBackground = null);
        return super.disableHoverBackground();
    }

    @Override
    public W disableHoverOverlay() {
        expectCount();
        forEachState(state -> state.hoverOverlay = null);
        return super.disableHoverOverlay();
    }

    protected W value(IIntValue<?> value) {
        setSyncOrValue(ISyncOrValue.orEmpty(value));
        return getThis();
    }

    @Override
    public W child(IWidget child) {
        this.fallbackChild = child;
        return super.child(child);
    }

    public W stateChild(int state, IWidget child) {
        getOrCreateState(state).child = child;
        return getThis();
    }

    /**
     * Sets the state dependent background. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public W stateBackground(UITexture texture) {
        expectCount();
        splitTexture(texture, (state, tex) -> state.background = tex);
        return getThis();
    }

    /**
     * Sets the state dependent overlay. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public W stateOverlay(UITexture texture) {
        expectCount();
        splitTexture(texture, (state, tex) -> state.overlay = tex);
        return getThis();
    }

    /**
     * Sets the state dependent hover background. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public W stateHoverBackground(UITexture texture) {
        expectCount();
        splitTexture(texture, (state, tex) -> state.hoverBackground = tex);
        return getThis();
    }

    /**
     * Sets the state dependent hover overlay. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public W stateHoverOverlay(UITexture texture) {
        expectCount();
        splitTexture(texture, (state, tex) -> state.hoverOverlay = tex);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, IDrawable tooltip) {
        getOrCreateState(state).tooltip(this).addDrawableLine(tooltip);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, String tooltip) {
        return addTooltip(state, Text.str(tooltip));
    }

    /**
     * Adds a tooltip element to all states.
     *
     * @param s element
     * @return this
     */
    @Override
    public W addTooltipElement(String s) {
        expectCount();
        forEachTooltip(t -> t.add(s));
        return getThis();
    }

    /**
     * Adds tooltip drawables as lines to all states.
     *
     * @param lines drawables
     * @return this
     */
    @Override
    public W addTooltipDrawableLines(Iterable<IDrawable> lines) {
        expectCount();
        forEachTooltip(t -> t.addDrawableLines(lines));
        return getThis();
    }

    /**
     * Adds a tooltip element to all states.
     *
     * @param drawable element
     * @return this
     */
    @Override
    public W addTooltipElement(IDrawable drawable) {
        expectCount();
        forEachTooltip(t -> t.addDrawable(drawable));
        return getThis();
    }

    /**
     * Adds a tooltip line to all states.
     *
     * @param line tooltip line
     * @return this
     */
    @Override
    public W addTooltipLine(ITextLine line) {
        expectCount();
        forEachTooltip(t -> t.addLine(line));
        return getThis();
    }

    /**
     * Adds a tooltip line to all states.
     *
     * @param drawable tooltip line
     * @return this
     */
    @Override
    public W addTooltipLine(IDrawable drawable) {
        expectCount();
        forEachTooltip(t -> t.addDrawableLine(drawable));
        return getThis();
    }

    /**
     * Adds tooltip lines to all states.
     *
     * @param lines tooltip lines
     * @return this
     */
    @Override
    public W addTooltipStringLines(Iterable<String> lines) {
        expectCount();
        forEachTooltip(t -> t.addStringLines(lines));
        return getThis();
    }

    /**
     * Applies a function to the tooltip of all states once.
     *
     * @param tooltipConsumer tooltip function
     * @return this
     */
    @Override
    public W tooltipStatic(Consumer<RichTooltip> tooltipConsumer) {
        expectCount();
        forEachTooltip(tooltipConsumer);
        return getThis();
    }

    /**
     * Applies a function to the tooltip of all states every time the tooltip needs to update.
     *
     * @param tooltipBuilder tooltip function
     * @return this
     */
    @Override
    public W tooltipDynamic(Consumer<RichTooltip> tooltipBuilder) {
        expectCount();
        forEachTooltip(t -> t.tooltipBuilder(tooltipBuilder));
        return getThis();
    }

    /**
     * Sets the tooltip alignment of all states.
     *
     * @param alignment alignment
     * @return this
     */
    @Override
    public W tooltipAlignment(Alignment alignment) {
        super.tooltipAlignment(alignment);
        expectCount();
        forEachTooltip(t -> t.alignment(alignment));
        return getThis();
    }

    /**
     * Sets the tooltip position of all states.
     *
     * @param pos tooltip pos
     * @return this
     */
    @Override
    public W tooltipPos(RichTooltip.Pos pos) {
        super.tooltipPos(pos);
        expectCount();
        forEachTooltip(t -> t.pos(pos));
        return getThis();
    }

    /**
     * Sets the tooltip position of all states.
     *
     * @param x x
     * @param y y
     * @return this
     */
    @Override
    public W tooltipPos(int x, int y) {
        super.tooltipPos(x, y);
        expectCount();
        forEachTooltip(t -> t.pos(x, y));
        return getThis();
    }

    /**
     * Sets the tooltip scale of all states.
     *
     * @param scale tooltip scale
     * @return this
     */
    @Override
    public W tooltipScale(float scale) {
        super.tooltipScale(scale);
        expectCount();
        forEachTooltip(t -> t.scale(scale));
        return getThis();
    }

    /**
     * Sets the tooltip text color of all states.
     *
     * @param textColor tooltip text color
     * @return this
     */
    @Override
    public W tooltipTextColor(int textColor) {
        super.tooltipTextColor(textColor);
        expectCount();
        forEachTooltip(t -> t.textColor(textColor));
        return getThis();
    }

    /**
     * Sets the tooltip text shadow of all states.
     *
     * @param textShadow tooltip pos
     * @return this
     */
    @Override
    public W tooltipTextShadow(boolean textShadow) {
        super.tooltipTextShadow(textShadow);
        expectCount();
        forEachTooltip(t -> t.textShadow(textShadow));
        return getThis();
    }

    /**
     * Sets the tooltip show up timer of all states.
     *
     * @param showUpTimer tooltip show up timer
     * @return this
     */
    @Override
    public W tooltipShowUpTimer(int showUpTimer) {
        super.tooltipShowUpTimer(showUpTimer);
        expectCount();
        forEachTooltip(t -> t.showUpTimer(showUpTimer));
        return getThis();
    }

    /**
     * Sets the tooltip auto update value for all states.
     *
     * @param update true if tooltips should automatically update
     * @return this
     */
    @Override
    public W tooltipAutoUpdate(boolean update) {
        super.tooltipAutoUpdate(update);
        expectCount();
        forEachTooltip(t -> t.autoUpdate(update));
        return getThis();
    }

    protected W stateCount(int stateCount) {
        updateStateCount(stateCount, true);
        return getThis();
    }

    private static IDrawable[] checkArray(IDrawable[] array, int length) {
        if (array == null) return new IDrawable[length];
        return array.length < length ? Arrays.copyOf(array, length) : array;
    }

    protected IDrawable[] addToArray(IDrawable[] array, IDrawable[] drawable, int index) {
        return addToArray(array, IDrawable.of(drawable), index);
    }

    protected IDrawable[] addToArray(IDrawable[] array, IDrawable drawable, int index) {
        if (index < 0) throw new IndexOutOfBoundsException();
        updateStateCount(index + 1, false);
        if (array == null || index >= array.length) {
            IDrawable[] copy = new IDrawable[(int) (Math.ceil((index + 1) / 4.0) * 4)];
            if (array != null) {
                System.arraycopy(array, 0, copy, 0, array.length);
            }
            array = copy;
        }
        array[index] = drawable;
        return array;
    }

    protected void splitTexture(UITexture texture, BiConsumer<State, UITexture> setter) {
        float a = 1f / this.stateCount;
        for (int i = 0; i < this.stateCount; i++) {
            setter.accept(getOrCreateState(i), texture.getSubArea(0, i * a, 1, i * a + a));
        }
    }

    protected W tooltip(int index, Consumer<RichTooltip> builder) {
        builder.accept(getOrCreateState(index).tooltip(this));
        return getThis();
    }

    protected W tooltipBuilder(int index, Consumer<RichTooltip> builder) {
        getOrCreateState(index).tooltip(this).tooltipBuilder(builder);
        return getThis();
    }

    public static class State {

        protected IDrawable background;
        protected IDrawable hoverBackground;
        protected IDrawable overlay;
        protected IDrawable hoverOverlay;
        private RichTooltip tooltip;
        protected IWidget child;

        private RichTooltip tooltip(IWidget widget) {
            if (this.tooltip == null) {
                this.tooltip = new RichTooltip().parent(widget);
            }
            return this.tooltip;
        }

        private void reset() {
            this.background = null;
            this.hoverBackground = null;
            this.overlay = null;
            this.hoverOverlay = null;
            this.child = null;
            if (this.tooltip != null) {
                this.tooltip.reset();
            }
        }

        public boolean hasTooltip() {
            return this.tooltip != null && !this.tooltip.isEmpty();
        }
    }
}
