package com.gregtechceu.gtceu.api.capability.data.query;

import com.gregtechceu.gtceu.api.capability.data.IComputationProvider;

public interface IComputationQuery extends IBridgeable {

    void registerProvider(IComputationProvider provider);
}
