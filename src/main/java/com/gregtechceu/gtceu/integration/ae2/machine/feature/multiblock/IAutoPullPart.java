package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import appeng.api.stacks.GenericStack;

import java.util.function.Predicate;

public interface IAutoPullPart {

    boolean isAutoPull();

    void setAutoPull(boolean autoPull);

    void setAutoPullTest(Predicate<GenericStack> test);
}
