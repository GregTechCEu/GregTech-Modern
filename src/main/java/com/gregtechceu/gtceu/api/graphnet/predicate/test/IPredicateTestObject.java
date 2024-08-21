package com.gregtechceu.gtceu.api.graphnet.predicate.test;

import org.jetbrains.annotations.UnknownNullability;

public interface IPredicateTestObject {

    IPredicateTestObject INSTANCE = new IPredicateTestObject() {};

    @UnknownNullability
    default Object recombine() {
        return null;
    }
}
