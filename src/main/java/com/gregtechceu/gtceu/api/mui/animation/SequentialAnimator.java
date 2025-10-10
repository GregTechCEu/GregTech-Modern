package com.gregtechceu.gtceu.api.mui.animation;

import com.gregtechceu.gtceu.GTCEu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequentialAnimator extends BaseAnimator implements IAnimator {

    private final List<IAnimator> animators;
    private int currentIndex = 0;

    public SequentialAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    public SequentialAnimator(IAnimator... animators) {
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
        if (this.animators.isEmpty()) return;
        super.animate(reverse);
        // start first animation
        this.animators.get(this.currentIndex).animate(reverse);
    }

    @Override
    public void reset(boolean atEnd) {
        this.currentIndex = atEnd ? this.animators.size() - 1 : 0;
        this.animators.forEach(animator -> animator.reset(atEnd));
    }

    @Override
    public int advance(int elapsedTime) {
        while (isAnimating() && elapsedTime > 0) {
            IAnimator animator = this.animators.get(currentIndex);
            elapsedTime = animator.advance(elapsedTime);
            if (!animator.isAnimating()) {
                // animator has finished
                this.currentIndex += getDirection();
                GTCEu.LOGGER.info("Finished {}th animator", this.currentIndex);
                if (this.currentIndex >= this.animators.size() || this.currentIndex < 0) {
                    // whole sequence has finished
                    stop(false);
                } else {
                    // start next animation
                    animator = this.animators.get(this.currentIndex);
                    animator.animate(isAnimatingReverse());
                }
            }
        }
        return elapsedTime;
    }

    @Override
    public boolean hasProgressed() {
        return !this.animators.isEmpty() && this.animators.get(0).hasProgressed();
    }

    @Override
    public SequentialAnimator followedBy(IAnimator animator) {
        if (isAnimating()) {
            throw new IllegalStateException("Can't add animators while animating");
        }
        reset();
        this.animators.add(animator);
        return this;
    }
}
