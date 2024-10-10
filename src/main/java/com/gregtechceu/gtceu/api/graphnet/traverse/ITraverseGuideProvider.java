package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

public interface ITraverseGuideProvider<N extends NetNode, P extends INetPath<N, ?>, T extends IPredicateTestObject> {

    @Nullable
    <D extends ITraverseData<N, P>> TraverseGuide<N, P, D> getGuide(
                                                                    TraverseDataProvider<D, T> provider, T testObject,
                                                                    long flow, boolean simulate);
}
