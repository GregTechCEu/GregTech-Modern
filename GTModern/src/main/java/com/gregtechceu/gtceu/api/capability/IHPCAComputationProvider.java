package com.gregtechceu.gtceu.api.capability;

public interface IHPCAComputationProvider extends IHPCAComponentHatch {

    /**
     * How much CWU/t this component can make, if it is being utilized fully.
     */
    int getCWUPerTick();

    /**
     * How much coolant/t this component needs when running at max CWU/t.
     */
    int getCoolingPerTick();
}
