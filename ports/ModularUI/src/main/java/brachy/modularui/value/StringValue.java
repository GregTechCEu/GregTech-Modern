package brachy.modularui.value;

import brachy.modularui.api.value.IStringValue;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringValue extends ObjectValue<String> implements IStringValue<String> {

    public static Dynamic wrap(IStringValue<?> val) {
        return new Dynamic(val::getStringValue, val::setStringValue);
    }

    public StringValue(String value) {
        super(String.class, value);
    }

    @Override
    public String getStringValue() {
        return getValue();
    }

    @Override
    public void setStringValue(String val) {
        setValue(val);
    }

    public static class Dynamic extends ObjectValue.Dynamic<String> implements IStringValue<String> {

        public Dynamic(Supplier<String> getter, @Nullable Consumer<String> setter) {
            super(String.class, getter, setter);
        }

        @Override
        public String getStringValue() {
            return getValue();
        }

        @Override
        public void setStringValue(String val) {
            setValue(val);
        }
    }
}
