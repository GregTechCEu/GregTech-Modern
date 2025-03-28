package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.TriPredicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BreadthFirstBlockSearch {

    public static Set<BlockPos> search(Predicate<BlockPos> value, BlockPos start, int limit) {
        Set<BlockPos> alreadyVisited = new HashSet<>();
        Set<BlockPos> valid = new LinkedHashSet<>();
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

    public static <T extends BlockEntity> Set<T> conditionalBlockEntitySearch(Class<T> clazz, T start,
                                                                              TriPredicate<T, T, Direction> condition,
                                                                              int blockLimit, int iterationLimit) {
        var level = start.getLevel();
        if (level == null) return Set.of();

        var passed = new LinkedHashSet<T>();
        var queue = new ObjectArrayFIFOQueue<Triple<T, T, Direction>>(16);
        queue.enqueue(new ImmutableTriple<>(null, start, null));

        var iterations = 0;
        while (!queue.isEmpty() && iterations < iterationLimit && passed.size() < blockLimit) {
            var tuple = queue.dequeue();
            var next = tuple.getMiddle();
            if (passed.contains(next)) {
                continue;
            }
            if (condition.test(tuple.getLeft(), tuple.getMiddle(), tuple.getRight())) {
                passed.add(next);
                for (var direction : Direction.values()) {
                    var neighbor = level.getBlockEntity(next.getBlockPos().relative(direction));
                    if (!clazz.isInstance(neighbor)) continue;
                    T casted = clazz.cast(neighbor);
                    if (passed.contains(casted)) continue;
                    queue.enqueue(new ImmutableTriple<>(next, casted, direction));
                }
            }
            iterations++;
        }

        return passed;
    }

    public static Set<BlockPos> conditionalBlockPosSearch(BlockPos start, BiPredicate<BlockPos, BlockPos> condition,
                                                          int blockLimit, int iterationLimit) {
        var passed = new LinkedHashSet<BlockPos>();
        var queue = new ObjectArrayFIFOQueue<Tuple<BlockPos, BlockPos>>(16);
        queue.enqueue(new Tuple<>(null, start));

        var iterations = 0;
        while (!queue.isEmpty() && iterations < iterationLimit && passed.size() < blockLimit) {
            var tuple = queue.dequeue();
            var next = tuple.getB();
            if (passed.contains(next)) {
                continue;
            }
            if (condition.test(tuple.getA(), tuple.getB())) {
                passed.add(next);
                getNeighbors(next).forEach(neighbor -> {
                    if (!passed.contains(neighbor)) {
                        queue.enqueue(new Tuple<>(next, neighbor));
                    }
                });
            }
            iterations++;
        }

        return passed;
    }
}
