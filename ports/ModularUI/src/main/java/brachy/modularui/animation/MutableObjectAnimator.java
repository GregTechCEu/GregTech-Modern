package brachy.modularui.animation;

import java.util.function.Consumer;

public class MutableObjectAnimator<T extends IAnimatable<T>> extends Animator {

    private final T from;
    private final T to;
    private final T animatable;
    private Consumer<T> intermediateConsumer;

    public MutableObjectAnimator(T animatable, T from, T to) {
        this.from = from;
        this.to = to;
        this.animatable = animatable;
        bounds(0f, 1f);
    }

    @Override
    public void resume(boolean reverse) {
        super.resume(reverse);
        this.animatable.interpolate(this.from, this.to, getRawValue());
    }

    @Override
    protected boolean onUpdate() {
        T intermediate = this.animatable.interpolate(this.from, this.to, getRawValue());
        if (this.intermediateConsumer != null) {
            this.intermediateConsumer.accept(intermediate);
        }
        return super.onUpdate();
    }

    public MutableObjectAnimator<T> intermediateConsumer(Consumer<T> consumer) {
        this.intermediateConsumer = consumer;
        return this;
    }
}
