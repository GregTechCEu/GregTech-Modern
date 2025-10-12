package com.gregtechceu.gtceu.api.mui.animation;

import org.jetbrains.annotations.Nullable;

public abstract class BaseAnimator<A extends BaseAnimator<A>> implements IAnimator {

    private IAnimator parent;
    protected boolean reverseOnFinish = false;
    protected int repeats = 0;

    private byte direction = 0;
    private boolean paused = false;
    private boolean startedReverse = false;
    private int repeated = 0;

    void setParent(IAnimator parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public A getThis() {
        return (A) this;
    }

    @Nullable
    public final IAnimator getParent() {
        return parent;
    }

    @Override
    public void reset(boolean atEnd) {
        this.startedReverse = atEnd;
        this.repeated = 0;
    }

    @Override
    public boolean stop(boolean force) {
        if (isAnimating() && !force) {
            if (this.reverseOnFinish && this.startedReverse == isAnimatingReverse()) {
                onAnimationFinished(false, false);
                // started reverse -> bounce back and animate forward
                animate(isAnimatingForward());
                return false;
            }
            if (repeats != 0 && (repeated < repeats || repeats < 0)) {
                onAnimationFinished(true, false);
                // started forward -> full cycle finished -> try repeating
                boolean reverse = !this.reverseOnFinish == isAnimatingReverse();
                animate(reverse);
                repeated++;
                return false;
            }
        }
        this.direction = 0;
        return true;
    }

    protected void onAnimationFinished(boolean finishedOneCycle, boolean finishedAllRepeats) {}

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

    /**
     * Sets if the animation should reverse animate once after it finished.
     * If the animation started in reverse it will animate forward on finish.
     *
     * @param reverseOnFinish if animation should bounce back on finish
     * @return this
     */
    public A reverseOnFinish(boolean reverseOnFinish) {
        this.reverseOnFinish = reverseOnFinish;
        return getThis();
    }

    /**
     * Sets how often the animation should repeat. If {@link #reverseOnFinish(boolean)} is set to true, it will repeat
     * the whole cycle.
     * If the number of repeats is negative, it will repeat infinitely.
     *
     * @param repeats how often the animation should repeat.
     * @return this
     */
    public A repeatsOnFinish(int repeats) {
        this.repeats = repeats;
        return getThis();
    }

    public SequentialAnimator followedBy(IAnimator animator) {
        return new SequentialAnimator(this, animator);
    }

    public ParallelAnimator inParallelWith(IAnimator animator) {
        return new ParallelAnimator(this, animator);
    }
}
