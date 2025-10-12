package com.gregtechceu.gtceu.api.mui.animation;

import net.minecraft.Util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IAnimator {

    @Nullable
    IAnimator getParent();

    default void animate(boolean reverse) {
        reset(reverse);
        resume(reverse);
    }

    default void animate() {
        animate(false);
    }

    boolean stop(boolean force);

    void pause();

    void resume(boolean reverse);

    void reset(boolean atEnd);

    default void reset() {
        reset(false);
    }

    /**
     * Advances the animation by a given duration.
     *
     * @param elapsedTime elapsed time in ms
     * @return remaining time (elapsed time - consumed time)
     */
    @ApiStatus.OverrideOnly
    int advance(int elapsedTime);

    boolean isPaused();

    boolean isAnimating();

    boolean isAnimatingReverse();

    boolean hasProgressed();

    default boolean isAnimatingForward() {
        return isAnimating() && !isAnimatingReverse();
    }

    static int getTimeDiff(long startTime) {
        return getTimeDiff(startTime, Util.getMillis());
    }

    static int getTimeDiff(long startTime, long currentTime) {
        long elapsedTime = Math.abs(currentTime - startTime);
        return (int) Math.min(Integer.MAX_VALUE, elapsedTime);
    }
}
