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
     * @return progress machine need to complete its stuff
     */
    int getMaxProgress();

    /**
     * @return current efficiency of machine
     */
    int getEfficiency();

    /**
     * @return max efficiency of machine
     */
    int getMaxEfficiency();

    /**
     * @return true is machine is active
     */
    boolean isActive();
}
