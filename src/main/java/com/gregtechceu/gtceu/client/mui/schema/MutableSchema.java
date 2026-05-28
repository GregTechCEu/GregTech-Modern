package com.gregtechceu.gtceu.client.mui.schema;

import brachy.modularui.schema.ISchema;
import brachy.modularui.utils.BlockPosUtil;
import brachy.modularui.utils.fakelevel.SchemaLevel;
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class MutableSchema implements ISchema {
    private final Level level = new SchemaLevel();
    private final BlockPos origin;
    private final Vector3f center;
    private @NotNull BiPredicate<BlockPos, BlockState> renderFilter = ($1, $2) -> true;
    private final Long2ReferenceMap<BlockState> blocks;

    public MutableSchema(Long2ReferenceMap<BlockState> blocks) {
        this.blocks = blocks;
        if (this.blocks.isEmpty()) {
            origin = BlockPos.ZERO;
            center = new Vector3f();
            return;
        }

        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        for (var entry : this.blocks.long2ReferenceEntrySet()) {
            if (entry.getValue().isAir()) continue;

            BlockPos pos = BlockPos.of(entry.getLongKey());
            getLevel().setBlockAndUpdate(pos, entry.getValue());
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterF(min, max);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public MutableSchema updateBlockState(BlockPos pos, BlockState state, BlockEntity entity) {
        this.blocks.put(pos.asLong(), state);
        getLevel().setBlockAndUpdate(pos, state);
        return this;
    }

    public MutableSchema updateBlockEntity(BlockEntity entity) {
        getLevel().setBlockEntity(entity);
        return this;
    }

    @Override
    public Vector3fc getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return origin;
    }

    @Override
    public void setRenderFilter(@NotNull BiPredicate<BlockPos, BlockState> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @NotNull BiPredicate<BlockPos, BlockState> getRenderFilter() {
        return this.renderFilter;
    }

    @Override
    public @NotNull Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return blocks.long2ReferenceEntrySet().stream()
                .map(e -> Map.entry(BlockPos.of(e.getLongKey()), e.getValue()))
                .iterator();
    }
}
