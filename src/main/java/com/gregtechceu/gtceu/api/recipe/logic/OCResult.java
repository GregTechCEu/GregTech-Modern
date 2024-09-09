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
    private int ocLevel;

    public void init(long eut, int duration, int ocLevel) {
        init(eut, duration, 0, ocLevel);
    }

    public void init(long eut, int duration, int parallel, int ocLevel) {
        init(eut, duration, parallel, parallel == 0 ? eut : eut * parallel, ocLevel);
    }

    public void init(long eut, int duration, int parallel, long parallelEUt, int ocLevel) {
        this.eut = eut;
        this.duration = duration;
        this.parallel = parallel;
        this.parallelEUt = parallelEUt;
        this.ocLevel = ocLevel;
    }

    public void reset() {
        this.eut = 0L;
        this.duration = 0;
        this.parallel = 0;
        this.parallelEUt = 0L;
        this.ocLevel = 0;
    }

    @Override
    public String toString() {
        return "OCResult[" +
                "EUt=" + eut + ", " +
                "duration=" + duration + ", " +
                "ocLevel=" + ocLevel + ", " +
                "parallel=" + parallel + ", " +
                "parallelEUt=" + parallelEUt + ']';
    }
}
