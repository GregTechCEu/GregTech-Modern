package brachy.modularui.utils;

import java.util.function.DoubleSupplier;

@FunctionalInterface
public interface FloatSupplier extends DoubleSupplier {

    float getAsFloat();

    @Override
    default double getAsDouble() {
        return getAsFloat();
    }
}
