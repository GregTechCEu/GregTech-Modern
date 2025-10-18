package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.api.mui.schema.ISchema;
import com.gregtechceu.gtceu.utils.BlockPosUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class MapSchema implements ISchema {

    @Getter
    private final Level level;
    private final Long2ReferenceOpenHashMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockState> renderFilter = (pos, state) -> true;
    // this isn't final because of constructor semantics but should be used as if it is
    @Getter
    private BlockPos origin;
    // this isn't final because of constructor semantics but should be used as if it is
    private Vec3 center;

    public MapSchema(Map<BlockPos, BlockState> blocks) {
        this(blocks, null);
    }

    // awful. oh well
    public MapSchema(Map<BlockPos, BlockState> blocks, BiPredicate<BlockPos, BlockState> renderFilter) {
        this(renderFilter);

        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        if (!blocks.isEmpty()) {
            for (var entry : blocks.entrySet()) {
                if (!entry.getValue().isAir()) {
                    BlockPos pos = entry.getKey();
                    this.blocks.put(pos.asLong(), entry.getValue());
                    this.level.setBlockAndUpdate(pos, entry.getValue());
                    BlockPosUtil.setMin(min, pos);
                    BlockPosUtil.setMax(max, pos);
                }
            }
        } else {
            min.set(0, 0, 0);
            max.set(0, 0, 0);
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterD(min, max);
    }

    public MapSchema(Long2ReferenceMap<BlockState> blocks) {
        this(blocks, null);
    }

    public MapSchema(Long2ReferenceMap<BlockState> blocks, BiPredicate<BlockPos, BlockState> renderFilter) {
        this(renderFilter);

        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        if (!blocks.isEmpty()) {
            for (var entry : blocks.long2ReferenceEntrySet()) {
                if (!entry.getValue().isAir()) {
                    BlockPos pos = BlockPos.of(entry.getLongKey());

                    this.blocks.put(entry.getLongKey(), entry.getValue());
                    this.level.setBlockAndUpdate(pos, entry.getValue());
                    BlockPosUtil.setMin(min, pos);
                    BlockPosUtil.setMax(max, pos);
                }
            }
        } else {
            min.set(0, 0, 0);
            max.set(0, 0, 0);
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterD(min, max);
    }

    protected MapSchema(BiPredicate<BlockPos, BlockState> renderFilter) {
        this.level = new SchemaLevel();
        if (renderFilter != null) {
            this.renderFilter = renderFilter;
        }
    }

    @Override
    public Vec3 getFocus() {
        return center;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return new AbstractIterator<>() {

            private final ObjectIterator<Long2ReferenceMap.Entry<BlockState>> it = blocks
                    .long2ReferenceEntrySet().fastIterator();

            @Override
            protected Map.Entry<BlockPos, BlockState> computeNext() {
                while (true) {
                    if (it.hasNext()) {
                        Long2ReferenceMap.Entry<BlockState> entry = it.next();
                        BlockPos key = BlockPos.of(entry.getLongKey());
                        if (renderFilter.test(key, entry.getValue())) {
                            return Map.entry(key, entry.getValue());
                        }
                        continue;
                    }
                    return endOfData();
                }
            }
        };
    }

    @Accessors(chain = true)
    public static class Builder {

        private final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
        @Setter
        private BiPredicate<BlockPos, BlockState> renderFilter;

        public Builder add(BlockPos pos, BlockState state) {
            if (state.isAir()) return this;
            this.blocks.put(pos.asLong(), state);
            return this;
        }

        public Builder add(Iterable<BlockPos> posList, Function<BlockPos, BlockState> function) {
            for (BlockPos pos : posList) {
                add(pos, function.apply(pos));
            }
            return this;
        }

        public Builder add(Map<BlockPos, BlockState> blocks) {
            blocks.forEach((pos, state) -> this.blocks.put(pos.asLong(), state));
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
