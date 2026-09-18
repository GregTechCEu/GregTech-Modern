package brachy.modularui.utils.serialization.codec;

public interface FieldWriter<T, V> {

    void writeField(T holder, V value);
}
