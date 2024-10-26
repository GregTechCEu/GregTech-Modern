package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.*;
import java.util.function.Predicate;

public class BreadthFirstBlockSearch {

    public static Set<BlockPos> search(Predicate<BlockPos> value, BlockPos start, int limit) {
        Set<BlockPos> alreadyVisited = new HashSet<>();
        Set<BlockPos> valid = new HashSet<>();
        int iteration = 0;

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        BlockPos currentNode;

        while (!queue.isEmpty()) {
            currentNode = queue.remove();

            if (value.test(currentNode)) {
                if (limit < iteration++) {
                    break;
                }
                valid.add(currentNode);
            } else {
                alreadyVisited.add(currentNode);
                queue.addAll(getNeighbors(currentNode));
                queue.removeAll(alreadyVisited);
            }
        }

        return valid;
    }

    public static Collection<BlockPos> getNeighbors(BlockPos pos) {
        Set<BlockPos> neighbors = new HashSet<>();
        for (Direction dir : GTUtil.DIRECTIONS) {
            neighbors.add(pos.relative(dir));
        }
        return neighbors;
    }
}
