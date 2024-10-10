package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseGuideProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseDataProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseGuide;

import org.jetbrains.annotations.Nullable;

public interface IFluidTraverseGuideProvider extends
                                             ITraverseGuideProvider<WorldPipeNetNode, FlowWorldPipeNetPath, FluidTestObject> {

    @Nullable
    @Override
    <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                        TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                        FluidTestObject testObject,
                                                                                                                                        long flow,
                                                                                                                                        boolean simulate);
}
