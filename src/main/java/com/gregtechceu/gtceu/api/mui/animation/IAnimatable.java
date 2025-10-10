package com.gregtechceu.gtceu.api.mui.animation;

import com.gregtechceu.gtceu.api.mui.base.drawable.IInterpolation;

public interface IAnimatable<T extends IAnimatable<T>> {

    T interpolate(T start, T end, float t);

    T copyOrImmutable();

    default boolean shouldAnimate(T target) {
        return !equals(target);
    }

    default MutableObjectAnimator<T> animator(T target) {
        T self = (T) this;
        return new MutableObjectAnimator<>(self, self.copyOrImmutable(), target);
    }

    default void animate(T target) {
        animate(target, false);
    }

    default void animate(T target, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).animate(reverse);
        }
    }

    default void animate(T target, boolean reverse, boolean reverseOnFinish, int repeatsOnFinish) {
        if (shouldAnimate(target)) {
            animator(target).reverseOnFinish(reverseOnFinish).repeatsOnFinish(repeatsOnFinish).animate(reverse);
        }
    }

    default void animate(T target, int durationMs, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).duration(durationMs).animate(reverse);
        }
    }

    default void animate(T target, IInterpolation curve, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).curve(curve).animate(reverse);
        }
    }

    default void animate(T target, IInterpolation curve, int durationMs, boolean reverse) {
        animate(target, curve, durationMs, reverse, false, 0);
    }

    default void animate(T target, IInterpolation curve, int durationMs, boolean reverse, boolean reverseOnFinish,
                         int repeatsOnFinish) {
        if (shouldAnimate(target)) {
            animator(target)
                    .curve(curve)
                    .duration(durationMs)
                    .reverseOnFinish(reverseOnFinish)
                    .repeatsOnFinish(repeatsOnFinish)
                    .animate(reverse);
        }
    }
}
