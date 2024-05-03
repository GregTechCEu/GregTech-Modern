package com.gregtechceu.gtceu.api.capability;

/**
 * For machines which have progress and can work
 */
public interface IWorkable extends IControllable {

    /**
     * @return current progress of machine
     */
    long getProgress();

    /**
     * @return progress machine need to complete it's stuff
     */
    long getMaxProgress();

    /**
     * @return true is machine is active
     */
    boolean isActive();

}