package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@FunctionalInterface
public interface TraverseDataProvider<D extends ITraverseData<?, ?>, T extends IPredicateTestObject> {

    D of(IGraphNet net, T testObject, SimulatorKey simulator, long queryTick,
         BlockPos sourcePos, Direction inputFacing);
}
