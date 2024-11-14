package com.gregtechceu.gtceu.api.recipe.logic;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class OCParams {

    private long eut;
    private int duration;
    private int ocAmount;

    public void initialize(long eut, int duration, int ocAmount) {
        this.eut = eut;
        this.duration = duration;
        this.ocAmount = ocAmount;
    }

    public void reset() {
        this.eut = 0L;
        this.duration = 0;
        this.ocAmount = 0;
    }
}
