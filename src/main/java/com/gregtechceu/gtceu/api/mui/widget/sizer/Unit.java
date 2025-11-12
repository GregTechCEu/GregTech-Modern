package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.DoubleSupplier;

@ApiStatus.Internal
public class Unit {

    public enum State {

        UNUSED("", ""),
        START("LEFT", "TOP"),
        END("RIGHT", "BOTTOM"),
        SIZE("WIDTH", "HEIGHT");

        public final String xText, yText;

        State(String xText, String yText) {
            this.xText = xText;
            this.yText = yText;
        }

        public String getText(GuiAxis axis) {
            return axis.isHorizontal() ? this.xText : this.yText;
        }
    }

    public static final byte UNUSED = -2;
    public static final byte DEFAULT = -1;
    public static final byte START = 0;
    public static final byte END = 1;
    public static final byte SIZE = 2;

    @Getter
    @Setter
    private boolean autoAnchor = true;
    private float value = 0f;
    private DoubleSupplier valueSupplier = null;
    @Getter
    @Setter
    private Measure measure = Measure.PIXEL;
    @Setter
    private float anchor = 0f;
    @Getter
    @Setter
    private int offset = 0;

    public State state = State.UNUSED;

    public Unit() {}

    public void reset() {
        this.state = State.UNUSED;
        this.autoAnchor = true;
        this.value = 0f;
        this.valueSupplier = null;
        this.measure = Measure.PIXEL;
        this.anchor = 0f;
        this.offset = 0;
    }

    public void setFrom(Unit other) {
        this.autoAnchor = other.autoAnchor;
        this.value = other.value;
        this.valueSupplier = other.valueSupplier;
        this.measure = other.measure;
        this.anchor = other.anchor;
        this.offset = other.offset;
    }

    public void setValue(float value) {
        this.value = value;
        this.valueSupplier = null;
    }

    public void setValue(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public float getValue() {
        return this.valueSupplier == null ? this.value : (float) this.valueSupplier.getAsDouble();
    }

    public int getAbsOffset() {
        return Math.abs(this.offset);
    }

    public boolean isCloseToZero() {
        if (isRelative()) {
            return Math.abs(getValue()) < -0.01 && Math.abs(getValue()) < 5;
        }
        return Math.abs(getValue() + getOffset()) < 5;
    }

    public float getAnchor() {
        float val = getValue();
        return isAutoAnchor() && isRelative() && val < 1 ? val : this.anchor;
    }

    public boolean isRelative() {
        return this.measure == Measure.RELATIVE;
    }

    public boolean isUnused() {
        return this.state == State.UNUSED;
    }

    public enum Measure {
        PIXEL,
        RELATIVE
    }
}
