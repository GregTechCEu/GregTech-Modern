package com.gregtechceu.gtceu.api.capability;

/**
 * For machines which have progress and can work
 */
public interface IWorkable extends IControllable {

    /**
     * @return current progress of machine
     */
    int getProgress();

    /**
     * @return progress machine need to complete it's stuff
     */
    int getMaxProgress();

    /**
     * @return true is machine is active
     */
    boolean isActive();

}