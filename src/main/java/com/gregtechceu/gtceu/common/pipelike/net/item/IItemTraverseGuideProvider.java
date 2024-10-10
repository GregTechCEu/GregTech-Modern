package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseGuideProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseDataProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseGuide;

import org.jetbrains.annotations.Nullable;

public interface IItemTraverseGuideProvider extends
                                            ITraverseGuideProvider<WorldPipeNetNode, FlowWorldPipeNetPath, ItemTestObject> {

    @Nullable
    @Override
    <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                        TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                        ItemTestObject testObject,
                                                                                                                                        long flow,
                                                                                                                                        boolean simulate);
}
