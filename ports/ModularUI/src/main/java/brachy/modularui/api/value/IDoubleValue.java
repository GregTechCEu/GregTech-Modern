package brachy.modularui.api.value;

public interface IDoubleValue<T> extends IValue<T> {

    double getDoubleValue();

    void setDoubleValue(double val);
}
