package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.drawable.IInterpolation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

@Accessors(chain = true)
public class Animator {

    private static final List<Animator> activeAnimators = new ArrayList<>();

    @ApiStatus.Internal
    public static void advance() {
        activeAnimators.removeIf(Animator::tick);
    }

    @Getter
    private final int duration;
    @Getter
    private int progress;
    private int dir = 0;
    @Getter
    private float min = 0, max = 1;
    @Getter
    private float value;
    private final IInterpolation interpolation;
    @Setter
    private DoublePredicate callback;
    @Setter
    private DoubleConsumer endCallback;

    public Animator(int duration, IInterpolation interpolation) {
        this.duration = duration;
        this.interpolation = interpolation;
    }

    @Tolerate
    public Animator setCallback(DoubleConsumer callback) {
        this.callback = val -> {
            callback.accept(val);
            return false;
        };
        return this;
    }

    public Animator setValueBounds(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public void forward() {
        this.progress = 0;
        this.dir = 1;
        updateValue();
        if (!activeAnimators.contains(this)) {
            activeAnimators.add(this);
        }
    }

    public void backward() {
        this.progress = this.duration;
        this.dir = -1;
        updateValue();
        if (!activeAnimators.contains(this)) {
            activeAnimators.add(this);
        }
    }

    public boolean isRunning() {
        return this.dir != 0 && (this.dir > 0 ? this.progress < this.duration : this.progress > 0);
    }

    public boolean isRunningForwards() {
        return this.dir > 0 && this.progress < this.duration;
    }

    public boolean isRunningBackwards() {
        return this.dir < 0 && this.progress > 0;
    }

    private boolean tick() {
        this.progress += this.dir;
        updateValue();
        if (this.callback != null && this.callback.test(this.value)) {
            this.dir = 0; // stop animation
        }
        if (!isRunning()) {
            if (this.endCallback != null) this.endCallback.accept(this.value);
            return true;
        }
        return false;
    }

    private void updateValue() {
        this.value = this.interpolation.interpolate(this.min, this.max, this.progress / (float) this.duration);
    }
}
