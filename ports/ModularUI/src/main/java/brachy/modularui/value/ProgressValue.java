package brachy.modularui.value;

import net.minecraft.util.Util;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class ProgressValue extends DoubleValue {

    @Getter private final int duration;

    public ProgressValue(int duration) {
        super(0);
        this.duration = duration;
    }

    public ProgressValue(long duration, TimeUnit unit) {
        this((int) unit.toMillis(duration));
    }

    @Override
    public double getDoubleValue() {
        setDoubleValue(Util.getMillis() % this.duration / (double) this.duration);
        return super.getDoubleValue();
    }
}
