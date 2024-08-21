package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface TraverseDataProvider<D extends ITraverseData<?, ?>, T extends IPredicateTestObject> {

    D of(IGraphNet net, T testObject, SimulatorKey simulator, long queryTick,
         BlockPos sourcePos, Direction inputFacing);
}
