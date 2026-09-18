package brachy.modularui.value;

import brachy.modularui.api.value.IIntValue;
import brachy.modularui.api.value.IShortValue;

public class ShortValue implements IShortValue<Short>, IIntValue<Short> {

    public static Dynamic wrap(IShortValue<?> val) {
        return new Dynamic(val::getShortValue, val::setShortValue);
    }

    protected short value;

    @Override
    public void setShortValue(short b) {
        value = b;
    }

    @Override
    public short getShortValue() {
        return value;
    }

    @Override
    public Short getValue() {
        return getShortValue();
    }

    @Override
    public void setValue(Short value) {
        setShortValue(value);
    }

    @Override
    public Class<Short> getValueType() {
        return Short.class;
    }

    @Override
    public int getIntValue() {
        return value;
    }

    @Override
    public void setIntValue(int val) {
        setShortValue((short) val);
    }

    public static class Dynamic extends ShortValue {

        private final Supplier getter;
        private final Consumer setter;

        public Dynamic(Supplier getter, Consumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public void setShortValue(short b) {
            this.setter.setShort(b);
        }

        @Override
        public short getShortValue() {
            return this.getter.getShort();
        }
    }

    public interface Supplier {

        short getShort();
    }

    public interface Consumer {

        void setShort(short b);
    }
}
