package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.IBoolValue;
import com.gregtechceu.gtceu.api.mui.base.value.IEnumValue;
import com.gregtechceu.gtceu.api.mui.base.value.IIntValue;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.value.IntValue;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AbstractCycleButtonWidget<W extends AbstractCycleButtonWidget<W>> extends Widget<W>
                                      implements Interactable {

    private int stateCount = 1;
    private IIntValue<?> intValue;
    private int lastValue = -1;
    protected IDrawable[] background = null;
    protected IDrawable[] hoverBackground = null;
    protected IDrawable[] overlay = null;
    protected IDrawable[] hoverOverlay = null;
    private final List<RichTooltip> stateTooltip = new ArrayList<>();

    @Override
    public void onInit() {
        if (this.intValue == null) {
            this.intValue = new IntValue(0);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.intValue = castIfTypeElseNull(syncHandler, IIntValue.class);
        return this.intValue != null;
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
        if (setSource) {
            this.intValue.setIntValue(state);
        }
        this.lastValue = state;
        markTooltipDirty();
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
    public WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public IDrawable getCurrentBackground(ITheme theme, WidgetTheme widgetTheme) {
        // make sure texture is up-to-date
        int state = getState();
        if (isHovering() && this.hoverBackground != null && this.hoverBackground[state] != null) {
            return this.hoverBackground[state];
        }
        return this.background != null && this.background[state] != null ? this.background[state] :
                super.getCurrentBackground(theme, widgetTheme);
    }

    @Override
    public IDrawable getCurrentOverlay(ITheme theme, WidgetTheme widgetTheme) {
        int state = getState();
        if (isHovering() && this.hoverOverlay != null && this.hoverOverlay[state] != null) {
            return this.hoverOverlay[state];
        }
        return this.overlay != null && this.overlay[state] != null ? this.overlay[state] :
                super.getCurrentOverlay(theme, widgetTheme);
    }

    @Override
    public boolean hasTooltip() {
        int state = getState();
        return super.hasTooltip() || (this.stateTooltip.size() > state && !this.stateTooltip.get(state).isEmpty());
    }

    @Override
    public void markTooltipDirty() {
        super.markTooltipDirty();
        for (RichTooltip tooltip : this.stateTooltip) {
            tooltip.markDirty();
        }
        getState();
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        RichTooltip tooltip = super.getTooltip();
        if (tooltip == null || tooltip.isEmpty()) {
            return this.stateTooltip.get(getState());
        }
        return tooltip;
    }

    protected W value(IIntValue<?> value) {
        this.intValue = value;
        setValue(value);
        if (value instanceof IEnumValue<?> enumValue) {
            stateCount(enumValue.getEnumClass().getEnumConstants().length);
        } else if (value instanceof IBoolValue) {
            stateCount(2);
        }
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
        splitTexture(texture, this.hoverOverlay);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, IDrawable tooltip) {
        if (state >= this.stateTooltip.size() || state < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.stateTooltip.get(state).addLine(tooltip);
        return getThis();
    }

    /**
     * Adds a line to the tooltip
     */
    protected W addTooltip(int state, String tooltip) {
        return addTooltip(state, IKey.str(tooltip));
    }

    protected W stateCount(int stateCount) {
        this.stateCount = stateCount;
        // adjust tooltip buffer size
        while (this.stateTooltip.size() < this.stateCount) {
            this.stateTooltip.add(new RichTooltip(this));
        }
        while (this.stateTooltip.size() > this.stateCount) {
            this.stateTooltip.remove(this.stateTooltip.size() - 1);
        }
        this.background = checkArray(this.background, stateCount);
        this.overlay = checkArray(this.overlay, stateCount);
        this.hoverBackground = checkArray(this.hoverBackground, stateCount);
        this.hoverOverlay = checkArray(this.hoverOverlay, stateCount);
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
        builder.accept(this.stateTooltip.get(index));
        return getThis();
    }

    protected W tooltipBuilder(int index, Consumer<RichTooltip> builder) {
        this.stateTooltip.get(index).tooltipBuilder(builder);
        return getThis();
    }
}
