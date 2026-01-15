package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget containing a floating point input field, as well as adjacent buttons for increasing or decreasing the value.
 *
 * <p>
 * The buttons' change amount can be altered with Ctrl, Shift, or both.<br>
 * The input is limited by a minimum and maximum value.
 * </p>
 */
public class FloatInputWidget extends NumberInputWidget<Float> {

    public FloatInputWidget(Supplier<Float> valueSupplier, Consumer<Float> onChanged) {
        super(valueSupplier, onChanged);
    }

    public FloatInputWidget(Position position, Supplier<Float> valueSupplier, Consumer<Float> onChanged) {
        super(position, valueSupplier, onChanged);
    }

    public FloatInputWidget(Position position, Size size, Supplier<Float> valueSupplier, Consumer<Float> onChanged) {
        super(position, size, valueSupplier, onChanged);
    }

    public FloatInputWidget(int x, int y, int width, int height, Supplier<Float> valueSupplier,
                            Consumer<Float> onChanged) {
        super(x, y, width, height, valueSupplier, onChanged);
    }

    @Override
    protected Float defaultMin() {
        return 0.0f;
    }

    @Override
    protected Float defaultMax() {
        return Float.MAX_VALUE;
    }

    @Override
    protected String toText(Float value) {
        return String.valueOf(value);
    }

    @Override
    protected Float fromText(String value) {
        return Float.parseFloat(value);
    }

    @Override
    protected ChangeValues<Float> getChangeValues() {
        return new ChangeValues<>(1.0f, 0.1f, 0.01f, 0.001f);
    }

    @Override
    protected Float add(Float a, Float b) {
        return a + b;
    }

    @Override
    protected Float multiply(Float a, Float b) {
        return a * b;
    }

    @Override
    protected Float clamp(Float value, Float min, Float max) {
        return Mth.clamp(value, min, max);
    }

    @Override
    protected void setTextFieldRange(TextFieldWidget textField, Float min, Float max) {
        textField.setNumbersOnly(min, max);
    }

    @Override
    protected Float getOne(boolean positive) {
        return positive ? 1.0f : -1.0f;
    }
}
