package brachy.modularui.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParallelAnimator extends BaseAnimator<ParallelAnimator> implements IAnimator {

    private final List<IAnimator> animators;
    private int waitTimeBetweenAnimators;

    private int startedAnimating = 0;
    private int finishedAnimating = 0;
    private int waitTime = 0;

    public ParallelAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    public ParallelAnimator(IAnimator... animators) {
        this.animators = new ArrayList<>();
        Collections.addAll(this.animators, animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    @Override
    public void animate(boolean reverse) {
        super.animate(reverse);
        if (this.waitTimeBetweenAnimators <= 0) {
            for (IAnimator animator : animators) {
                animator.animate(reverse);
            }
            this.startedAnimating = this.animators.size();
        } else {
            this.animators.get(this.startedAnimating).animate(reverse);
        }
    }

    @Override

    public boolean stop(boolean force) {
        if (super.stop(force)) {
            for (IAnimator animator : animators) {
                animator.stop(force);
            }
            return true;
        }
        return false;
    }

    @Override
    public void reset(boolean atEnd) {
        super.reset(atEnd);
        this.startedAnimating = 0;
        this.finishedAnimating = 0;
        for (IAnimator animator : animators) {
            animator.reset(atEnd);
        }
    }

    @Override
    public int advance(int elapsedTime) {
        int remainingTime = 0;
        for (int i = 0; i < this.startedAnimating; i++) {
            IAnimator animator = this.animators.get(i);
            if (!animator.isAnimating()) continue;
            remainingTime = Math.max(remainingTime, animator.advance(elapsedTime));
            if (!animator.isAnimating()) {
                this.finishedAnimating++;
                if (isFinished()) {
                    stop(false);
                    return remainingTime;
                }
            }
        }
        while (elapsedTime > 0 && this.startedAnimating < this.animators.size()) {
            int prog = Math.min(elapsedTime, this.waitTimeBetweenAnimators - this.waitTime);
            this.waitTime += prog;
            elapsedTime -= prog;
            if (this.waitTime >= this.waitTimeBetweenAnimators) {
                this.animators.get(this.startedAnimating).animate(isAnimatingReverse());
                this.waitTime -= this.waitTimeBetweenAnimators;
                this.startedAnimating++;
            }
        }
        return Math.min(elapsedTime, remainingTime);
    }

    public boolean isFinished() {
        return this.finishedAnimating == this.animators.size();
    }

    @Override
    public boolean hasProgressed() {
        return isAnimating() && this.startedAnimating > 0;
    }

    public ParallelAnimator waitTimeBetweenAnimators(int waitTime) {
        this.waitTimeBetweenAnimators = waitTime;
        return this;
    }

    @Override
    public ParallelAnimator inParallelWith(IAnimator animator) {
        if (isAnimating()) {
            throw new IllegalStateException("Can't add animators while animating");
        }
        reset();
        this.animators.add(animator);
        return this;
    }
}
