package brachy.modularui.widget.sizer;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.function.DoubleSupplier;

@ApiStatus.Internal
public class Unit {

    public static final MutableObjectCodec<Unit> CODEC = MutableObjectCodec.builder(Unit::new)
            .addOpt("autoAnchor", Unit::setAutoAnchor, Unit::isAutoAnchor, Codec.BOOL, true)
            .addOpt("value", Unit::setValue, Unit::getValue, Codec.FLOAT, 0f)
            .addOpt("measure", Unit::setMeasure, Unit::getMeasure, Measure.CODEC, Measure.PIXEL)
            .addOpt("anchor", Unit::setAnchor, Unit::getAnchor, Codec.FLOAT, 0f)
            .addOpt("offset", Unit::setOffset, Unit::getOffset, Codec.INT, 0)
            .addOpt("state", Unit::setState, Unit::getState, State.CODEC, State.UNUSED).neverEncode()
            .addUnencodableOpt("valueSupplier", Unit::setValue, Unit::getValueSupplier, null)
            .build();

    static final Unit ZERO = new Unit();

    @Getter
    @Setter
    private boolean autoAnchor;
    private float value;
    @Getter(AccessLevel.PRIVATE)
    private DoubleSupplier valueSupplier;
    @Getter
    @Setter
    private Measure measure;
    @Setter
    private float anchor;
    @Getter
    @Setter
    private int offset;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    public State state;

    public Unit() {
        reset();
    }

    public void reset() {
        this.state = State.UNUSED;
        this.autoAnchor = true;
        this.value = 0f;
        this.valueSupplier = null;
        this.measure = Measure.PIXEL;
        this.anchor = 0f;
        this.offset = 0;
    }

    public void copyPropertiesOf(Unit other) {
        copyPropertiesOf(other, false);
    }

    private void copyPropertiesOf(Unit other, boolean copyState) {
        if (copyState) this.state = other.state;
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

    public boolean isEqual(Unit o) {
        return o != null &&
                this.autoAnchor == o.autoAnchor &&
                Float.compare(this.value, o.value) == 0 &&
                Float.compare(this.anchor, o.anchor) == 0 &&
                this.offset == o.offset &&
                this.measure == o.measure &&
                this.valueSupplier == o.valueSupplier &&
                this.state == o.state;
    }

    public static boolean areEqual(Unit a, Unit b) {
        return a == null ? b == null : a.isEqual(b);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == Unit.class && isEqual((Unit) obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoAnchor, value, valueSupplier, measure, anchor, offset, state);
    }

    public enum Measure implements StringRepresentable {

        PIXEL,
        RELATIVE;

        public static final Codec<Measure> CODEC = StringRepresentable.fromEnum(Measure::values);

        public final String name;

        Measure() {
            this.name = name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public enum State implements StringRepresentable {

        UNUSED("", ""),
        START("LEFT", "TOP"),
        END("RIGHT", "BOTTOM"),
        SIZE("WIDTH", "HEIGHT");

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        public final String name, xText, yText;

        State(String xText, String yText) {
            this.name = name().toLowerCase(Locale.ENGLISH);
            this.xText = xText;
            this.yText = yText;
        }

        public String getText(GuiAxis axis) {
            return axis.isHorizontal() ? this.xText : this.yText;
        }


        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
