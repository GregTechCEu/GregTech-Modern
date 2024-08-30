package com.gregtechceu.gtceu.api.recipe.logic;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class OCResult {

    private long eut;
    private long parallelEUt;
    private int duration;
    private int parallel;

    public void init(long eut, int duration) {
        init(eut, duration, 0);
    }

    public void init(long eut, int duration, int parallel) {
        init(eut, duration, parallel, parallel == 0 ? eut : eut * parallel);
    }

    public void init(long eut, int duration, int parallel, long parallelEUt) {
        this.eut = eut;
        this.duration = duration;
        this.parallel = parallel;
        this.parallelEUt = parallelEUt;
    }

    public void reset() {
        this.eut = 0L;
        this.duration = 0;
        this.parallel = 0;
        this.parallelEUt = 0L;
    }

    @Override
    public String toString() {
        return "OCResult[" +
                "EUt=" + eut + ", " +
                "duration=" + duration + ", " +
                "parallel=" + parallel + ", " +
                "parallelEUt=" + parallelEUt + ']';
    }
}
