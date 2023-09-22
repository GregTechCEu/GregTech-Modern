package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * FloodFiller3D borrowed from Ad Astra.
 * <a href="https://github.com/terrarium-earth/Ad-Astra/blob/1.19/common/src/main/java/earth/terrarium/ad_astra/common/util/algorithm/FloodFiller3D.java">github link</a>
 * modified to only test for air blocks, stopping on anything not air, and to be 4-way, instead of having a direction.
 */
public class FloodFiller3D {
    private static final Direction[] VALUES = Direction.values();

    public static Set<BlockPos> run(LevelReader level, BlockPos start) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        Set<BlockPos> queue = new LinkedHashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            if (positions.size() >= 256) break;

            var iterator = queue.iterator();
            var current = iterator.next();
            for (Direction dir : VALUES) {
                BlockPos pos = current.relative(dir);
                BlockState state = level.getBlockState(pos);

                if (state.getMaterial().blocksMotion()) continue;

                positions.add(pos);
            }
            iterator.remove();
        }

        return positions;
    }
}
