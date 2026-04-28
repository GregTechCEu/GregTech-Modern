package com.gregtechceu.gtceu.client.color;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.color.block.BlockTintSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class GTBlockTintSources {

    private static final int MAX_LEGACY_TINT_LAYERS = 16;

    private GTBlockTintSources() {}

    @SuppressWarnings("unchecked")
    public static List<BlockTintSource> adapt(Object value) {
        if (value instanceof Supplier<?> supplier) {
            return adapt(supplier.get());
        }
        if (value instanceof List<?> list) {
            return (List<BlockTintSource>) list;
        }
        if (value instanceof BlockTintSource tintSource) {
            return List.of(tintSource);
        }
        if (value instanceof BlockColor blockColor) {
            return fromBlockColor(blockColor);
        }
        throw new IllegalArgumentException("Unsupported block tint source: " + value);
    }

    public static List<BlockTintSource> fromBlockColor(BlockColor blockColor) {
        List<BlockTintSource> sources = new ArrayList<>(MAX_LEGACY_TINT_LAYERS);
        for (int tintIndex = 0; tintIndex < MAX_LEGACY_TINT_LAYERS; tintIndex++) {
            sources.add(fromBlockColor(blockColor, tintIndex));
        }
        return List.copyOf(sources);
    }

    public static BlockTintSource fromBlockColor(BlockColor blockColor, int tintIndex) {
        return new BlockTintSource() {

            @Override
            public int color(net.minecraft.world.level.block.state.BlockState state) {
                return GTUtil.convertRGBtoARGB(blockColor.getColor(state, null, null, tintIndex));
            }

            @Override
            public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                    net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                    net.minecraft.core.BlockPos pos) {
                return GTUtil.convertRGBtoARGB(blockColor.getColor(state, level, pos, tintIndex));
            }

            @Override
            public int colorAsTerrainParticle(net.minecraft.world.level.block.state.BlockState state,
                                              net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                              net.minecraft.core.BlockPos pos) {
                return GTUtil.convertRGBtoARGB(blockColor.getColor(state, level, pos, tintIndex));
            }
        };
    }
}
