package com.gregtechceu.gtceu.api.machine.trait.optical;

public enum ComputationType {
    /**
     * CWU/t works like EU/t. If there is not enough, recipe reverts progress/halts
     */
    STEADY,
    /**
     * CWU/t works like a total input. If there is not enough, recipe halts at current progress time.
     * Progress only increases on ticks where enough computation is present. Energy will always be drawn.
     */
    SPORADIC
}
