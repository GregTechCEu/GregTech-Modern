package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class MapSchema implements ISchema {

    @Getter
    private final Level level;
    private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> blocks = new Object2ObjectOpenHashMap<>();
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    @Getter
    private final BlockPos origin;
    private final Vec3 center;

    public MapSchema(Map<BlockPos, BlockInfo> blocks) {
        this(blocks, null);
    }

    public MapSchema(Map<BlockPos, BlockInfo> blocks, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.level = new DummyLevel();
        this.renderFilter = renderFilter;
        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        if (!blocks.isEmpty()) {
            for (var entry : blocks.entrySet()) {
                if (entry.getValue().getBlockState().getBlock() != Blocks.AIR) {
                    this.blocks.put(entry.getKey(), entry.getValue());
                    entry.getValue().apply(this.level, entry.getKey());
                    BlockPosUtil.setMin(min, entry.getKey());
                    BlockPosUtil.setMax(max, entry.getKey());
                }
            }
        } else {
            min.set(0, 0, 0);
            max.set(0, 0, 0);
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterD(min, max);
    }

    @Override
    public Vec3 getFocus() {
        return center;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final ObjectIterator<Object2ObjectMap.Entry<BlockPos, BlockInfo>> it = blocks
                    .object2ObjectEntrySet().fastIterator();

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                while (it.hasNext()) {
                    Map.Entry<BlockPos, BlockInfo> entry = it.next();
                    if (renderFilter == null || renderFilter.test(entry.getKey(), entry.getValue())) {
                        return entry;
                    }
                }
                return endOfData();
            }
        };
    }

    public static class Builder {

        private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> blocks = new Object2ObjectOpenHashMap<>();
        private BiPredicate<BlockPos, BlockInfo> renderFilter;

        public Builder add(BlockPos pos, BlockState state) {
            return add(pos, state, null);
        }

        public Builder add(BlockPos pos, BlockState state, BlockEntity customTile) {
            if (state.getBlock() == Blocks.AIR) return this;
            this.blocks.put(pos, new BlockInfo(state, customTile));
            return this;
        }

        public Builder add(BlockPos pos, BlockInfo blockInfo) {
            this.blocks.put(pos, blockInfo.toImmutable());
            return this;
        }

        public Builder add(Iterable<BlockPos> posList, Function<BlockPos, BlockInfo> function) {
            for (BlockPos pos : posList) {
                BlockInfo info = function.apply(pos).toImmutable();
                add(pos, info);
            }
            return this;
        }

        public Builder add(Map<BlockPos, BlockInfo> blocks) {
            this.blocks.putAll(blocks);
            return this;
        }

        public Builder setRenderFilter(BiPredicate<BlockPos, BlockInfo> renderFilter) {
            this.renderFilter = renderFilter;
            return this;
        }

        public MapSchema build() {
            if (renderFilter == null) {
                return new MapSchema(this.blocks);
            }
            return new MapSchema(this.blocks, renderFilter);
        }
    }
}
