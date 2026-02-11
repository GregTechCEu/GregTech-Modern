package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.drawable.ITextLine;
import com.gregtechceu.gtceu.api.mui.base.value.*;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.IntValue;
import com.gregtechceu.gtceu.api.mui.widget.SingleChildWidget;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public class AbstractCycleButtonWidget<W extends AbstractCycleButtonWidget<W>> extends SingleChildWidget<W>
                                      implements Interactable {

    private static final RichTooltip[] EMPTY_TOOLTIP = new RichTooltip[0];

    private int stateCount = 1;
    private boolean explicitStateCount = false;
    private boolean hasCount = false;
    private IIntValue<?> intValue;
    private int lastValue = -1;
    protected IDrawable[] background = null;
    protected IDrawable[] hoverBackground = null;
    protected IDrawable[] overlay = null;
    protected IDrawable[] hoverOverlay = null;
    protected RichTooltip[] tooltip = EMPTY_TOOLTIP;
    protected IWidget[] stateChildren = null;
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

        int currentSize = this.tooltip.length;
        if (stateCount > currentSize) {
            this.tooltip = Arrays.copyOf(this.tooltip, stateCount);
            for (; currentSize < stateCount; currentSize++) {
                this.tooltip[currentSize] = new RichTooltip().parent(this);
            }
        } else if (stateCount < currentSize) {
            for (int i = stateCount; i < currentSize; i++) {
                this.tooltip[i].reset();
            }
        }

        this.background = checkArray(this.background, stateCount);
        this.overlay = checkArray(this.overlay, stateCount);
        this.hoverBackground = checkArray(this.hoverBackground, stateCount);
        this.hoverOverlay = checkArray(this.hoverOverlay, stateCount);
        if (this.stateChildren == null) this.stateChildren = new IWidget[stateCount];
        else if (this.stateChildren.length < stateCount)
            this.stateChildren = Arrays.copyOf(this.stateChildren, stateCount);
    }

    protected void expectCount() {
        if (!this.hasCount) {
            GTCEu.LOGGER.error("State count for widget {} is required, but has not been set yet!", this);
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
        IWidget child = this.stateChildren != null && this.stateChildren.length > state ? this.stateChildren[state] :
                null;
        if (child != null) {
            child(child);
        } else if (getChild() != this.fallbackChild) {
            child(this.fallbackChild);
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
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
    public IDrawable getCurrentBackground(ITheme theme, WidgetThemeEntry<?> widgetTheme) {
        // make sure texture is up-to-date
        int state = getState();
        if (isHovering() && this.hoverBackground != null && this.hoverBackground[state] != null &&
                this.hoverBackground[state] != IDrawable.NONE) {
            return this.hoverBackground[state];
        }
        return this.background != null && this.background[state] != null ? this.background[state] :
                super.getCurrentBackground(theme, widgetTheme);
    }

    @Override
    public IDrawable getCurrentOverlay(ITheme theme, WidgetThemeEntry<?> widgetTheme) {
        int state = getState();
        if (isHovering() && this.hoverOverlay != null && this.hoverOverlay[state] != null &&
                this.hoverOverlay[state] != IDrawable.NONE) {
            return this.hoverOverlay[state];
        }
        return this.overlay != null && this.overlay[state] != null ? this.overlay[state] :
                super.getCurrentOverlay(theme, widgetTheme);
    }

    @Override
    public boolean hasTooltip() {
        int state = getState();
        return super.hasTooltip() || (this.tooltip.length > state && !this.tooltip[state].isEmpty());
    }

    @Override
    public void markTooltipDirty() {
        super.markTooltipDirty();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.markDirty();
        }
        getState();
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        RichTooltip tooltip = super.getTooltip();
        if (tooltip == null || tooltip.isEmpty()) {
            int state = getState();
            if (this.tooltip.length > 0 && this.tooltip.length > state) {
                return this.tooltip[state];
            }
        }
        return tooltip;
    }

    @Override
    public W disableHoverBackground() {
        expectCount();
        if (this.hoverBackground != null) {
            Arrays.fill(this.hoverBackground, IDrawable.NONE);
        }
        if (getHoverBackground() == null) {
            super.hoverBackground(IDrawable.NONE);
        }
        return getThis();
    }

    @Override
    public W disableHoverOverlay() {
        expectCount();
        if (this.hoverOverlay != null) {
            Arrays.fill(this.hoverOverlay, IDrawable.NONE);
        }
        if (getHoverOverlay() == null) {
            super.hoverOverlay(IDrawable.NONE);
        }
        return getThis();
    }

    @Override
    public W invisible() {
        if (this.background != null) {
            Arrays.fill(this.background, IDrawable.EMPTY);
        }
        if (getBackground() == null) {
            super.background(IDrawable.EMPTY);
        }
        return disableHoverBackground();
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
        updateStateCount(state, false);
        if (this.stateChildren == null) {
            this.stateChildren = new IWidget[state + 1];
        } else if (this.stateChildren.length < state + 1) {
            this.stateChildren = Arrays.copyOf(this.stateChildren, state + 1);
        }
        this.stateChildren[state] = child;
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
        splitTexture(texture, this.background);
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
        splitTexture(texture, this.overlay);
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
        splitTexture(texture, this.hoverBackground);
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
        splitTexture(texture, this.hoverOverlay);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, IDrawable tooltip) {
        updateStateCount(state + 1, false);
        this.tooltip[state].addLine(tooltip);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, String tooltip) {
        return addTooltip(state, IKey.str(tooltip));
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.add(s);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.addDrawableLines(lines);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.add(drawable);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.addLine(line);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.addLine(drawable);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.addStringLines(lines);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltipConsumer.accept(tooltip);
        }
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
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.tooltipBuilder(tooltipBuilder);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.alignment(alignment);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.pos(pos);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.pos(x, y);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.scale(scale);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.textColor(textColor);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.textShadow(textShadow);
        }
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
        expectCount();
        for (RichTooltip tooltip : this.tooltip) {
            tooltip.showUpTimer(showUpTimer);
        }
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

    protected static void splitTexture(UITexture texture, IDrawable[] dest) {
        for (int i = 0; i < dest.length; i++) {
            float a = 1f / dest.length;
            dest[i] = texture.getSubArea(0, i * a, 1, i * a + a);
        }
    }

    protected W tooltip(int index, Consumer<RichTooltip> builder) {
        updateStateCount(index + 1, false);
        builder.accept(this.tooltip[index]);
        return getThis();
    }

    protected W tooltipBuilder(int index, Consumer<RichTooltip> builder) {
        updateStateCount(index + 1, false);
        this.tooltip[index].tooltipBuilder(builder);
        return getThis();
    }
}
