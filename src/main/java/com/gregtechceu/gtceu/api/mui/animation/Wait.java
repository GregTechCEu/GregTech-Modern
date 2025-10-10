package com.gregtechceu.gtceu.api.mui.animation;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class Wait extends BaseAnimator {

    @Setter
    private int duration;
    private int progress = 0;

    public Wait() {
        this(250);
    }

    public Wait(int duration) {
        this.duration = duration;
    }

    @Override
    public void reset(boolean atEnd) {
        this.progress = 0;
    }

    @Override
    public int advance(int elapsedTime) {
        int max = this.duration - this.progress;
        int prog = Math.min(max, elapsedTime);
        this.progress += prog;
        if (this.progress >= this.duration) {
            stop(false);
        }
        return elapsedTime - prog;
    }

    @Override
    public boolean hasProgressed() {
        return progress > 0 && isAnimating();
    }
}
