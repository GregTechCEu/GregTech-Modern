package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget containing an integer input field, as well as adjacent buttons for increasing or decreasing the value.
 *
 * <p>
 * The buttons' change amount can be altered with Ctrl, Shift, or both.<br>
 * The input is limited by a minimum and maximum value.
 * </p>
 */
public class LongInputWidget extends NumberInputWidget<Long> {

    public LongInputWidget(Supplier<Long> valueSupplier, Consumer<Long> onChanged) {
        super(valueSupplier, onChanged);
    }

    public LongInputWidget(Position position, Supplier<Long> valueSupplier, Consumer<Long> onChanged) {
        super(position, valueSupplier, onChanged);
    }

    public LongInputWidget(Position position, Size size, Supplier<Long> valueSupplier, Consumer<Long> onChanged) {
        super(position, size, valueSupplier, onChanged);
    }

    public LongInputWidget(int x, int y, int width, int height, Supplier<Long> valueSupplier,
                           Consumer<Long> onChanged) {
        super(x, y, width, height, valueSupplier, onChanged);
    }

    @Override
    protected Long defaultMin() {
        return 0L;
    }

    @Override
    protected Long defaultMax() {
        return Long.MAX_VALUE;
    }

    @Override
    protected String toText(Long value) {
        return String.valueOf(value);
    }

    @Override
    protected Long fromText(String value) {
        return Long.parseLong(value);
    }

    @Override
    protected ChangeValues<Long> getChangeValues() {
        return new ChangeValues<>(1L, 8L, 64L, 512L);
    }

    @Override
    protected Long add(Long a, Long b) {
        return a + b;
    }

    @Override
    protected Long multiply(Long a, Long b) {
        return a * b;
    }

    @Override
    protected Long clamp(Long value, Long min, Long max) {
        return GTMath.clamp(value, min, max);
    }

    @Override
    protected void setTextFieldRange(TextFieldWidget textField, Long min, Long max) {
        textField.setNumbersOnly(min, max);
    }

    @Override
    protected Long getOne(boolean positive) {
        return positive ? 1L : -1L;
    }
}
