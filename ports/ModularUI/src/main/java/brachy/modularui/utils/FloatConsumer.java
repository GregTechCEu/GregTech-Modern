package brachy.modularui.utils;

import java.util.function.DoubleConsumer;

@FunctionalInterface
public interface FloatConsumer extends DoubleConsumer {

    void accept(float value);

    default void accept(double value) {
        accept((float) value);
    }
}
