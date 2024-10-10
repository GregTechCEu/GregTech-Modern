package com.gregtechceu.gtceu.api.capability.data;

public interface IComputationProvider {

    /**
     * Returns whether this provider supports bridging. If false, CWU will not be requested through
     * network switches with multiple inputs.
     * 
     * @return whether bridging is supported.
     */
    boolean supportsBridging();

    /**
     * Called to supply CWU to a requester.
     * 
     * @param requested the requested CWU
     * @param simulate  whether to simulate the request
     * @return the amount of CWU supplied.
     */
    long supplyCWU(long requested, boolean simulate);

    /**
     * @return the maximum CWU that can be supplied in a single tick.
     */
    long maxCWUt();
}
