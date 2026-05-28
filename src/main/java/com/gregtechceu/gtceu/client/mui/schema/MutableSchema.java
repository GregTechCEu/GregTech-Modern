package com.gregtechceu.gtceu.client.mui.schema;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.schema.ISchema;
import brachy.modularui.utils.BlockPosUtil;
import brachy.modularui.utils.fakelevel.SchemaLevel;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class MutableSchema implements ISchema {

    private final Level level = new SchemaLevel();
    private @NotNull BlockPos origin = BlockPos.ZERO;
    private @NotNull Vector3f center = new Vector3f();
    private @NotNull BiPredicate<BlockPos, BlockState> renderFilter = ($1, $2) -> true;
    @Getter
    private final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();

    public MutableSchema() {}

    public MutableSchema(Long2ReferenceMap<BlockState> blocks) {
        this();
        putAll(blocks);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public MutableSchema putAll(Long2ReferenceMap<BlockState> blocks) {
        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        for (long l : blocks.keySet()) {
            if (blocks.get(l).isAir()) continue;
            this.blocks.put(l, blocks.get(l));

            BlockPos pos = BlockPos.of(l);
            getLevel().setBlockAndUpdate(pos, blocks.get(l));
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);
        }
        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterF(min, max);
        return this;
    }

    public MutableSchema updateBlockState(BlockPos pos, BlockState state) {
        this.blocks.put(pos.asLong(), state);
        getLevel().setBlockAndUpdate(pos, state);
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
