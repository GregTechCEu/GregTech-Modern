package com.gregtechceu.gtceu.api.mui.animation;

import org.jetbrains.annotations.Nullable;

public abstract class BaseAnimator implements IAnimator {

    private IAnimator parent;

    private byte direction = 0;
    private boolean paused = false;

    void setParent(IAnimator parent) {
        this.parent = parent;
    }

    @Nullable
    public final IAnimator getParent() {
        return parent;
    }

    @Override
    public void stop(boolean force) {
        this.direction = 0;
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public void resume(boolean reverse) {
        this.paused = false;
        this.direction = (byte) (reverse ? -1 : 1);
        if (this.parent == null) AnimatorManager.startAnimation(this);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean isAnimating() {
        return this.direction != 0;
    }

    @Override
    public boolean isAnimatingReverse() {
        return this.direction < 0;
    }

    @Override
    public boolean isAnimatingForward() {
        return this.direction > 0;
    }

    public final byte getDirection() {
        return direction;
    }

    public SequentialAnimator followedBy(IAnimator animator) {
        return new SequentialAnimator(this, animator);
    }

    public ParallelAnimator inParallelWith(IAnimator animator) {
        return new ParallelAnimator(this, animator);
    }
}
