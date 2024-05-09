package com.gregtechceu.gtceu.api.machines.feature;

/**
 * @author KilaBash
 * @date 2023/3/22
 * @implNote IMufflableMachine
 */
public interface IMufflableMachine extends IMachineFeature {
    boolean isMuffled();
    void setMuffled(boolean isMuffled);
}
