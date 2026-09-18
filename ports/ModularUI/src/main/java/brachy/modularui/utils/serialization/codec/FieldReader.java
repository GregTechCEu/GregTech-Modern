package brachy.modularui.utils.serialization.codec;

public interface FieldReader<T, V> {

    V readField(T holder);
}
