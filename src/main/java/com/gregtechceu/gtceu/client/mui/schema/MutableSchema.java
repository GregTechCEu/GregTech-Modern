package com.gregtechceu.gtceu.client.mui.schema;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.schema.ISchema;
import brachy.modularui.utils.BlockPosUtil;
import brachy.modularui.utils.fakelevel.SchemaLevel;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class MutableSchema implements ISchema {

    protected final Level level = new SchemaLevel();
    protected @NotNull BlockPos origin = BlockPos.ZERO;
    protected @NotNull Vector3f center = new Vector3f();
    protected @NotNull BiPredicate<BlockPos, BlockState> renderFilter = ($1, $2) -> true;
    @Getter
    protected final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();

    public MutableSchema() {}

    public MutableSchema(Long2ReferenceMap<BlockState> blocks) {
        this();
        setBlocks(blocks);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public MutableSchema setBlocks(Long2ReferenceMap<BlockState> blocks) {
        this.blocks.clear();
        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        for (long l : blocks.keySet()) {
            if (blocks.get(l).isAir()) continue;
            BlockState block = blocks.get(l);
            BlockPos pos = BlockPos.of(l);
            this.blocks.put(l, block);
            getLevel().setBlockAndUpdate(pos, block);
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);

            if(block.getBlock() instanceof EntityBlock entityBlock){
                BlockEntity newEntity = entityBlock.newBlockEntity(pos, block);
                if(newEntity == null){
                    GTCEu.LOGGER.error("Could not create BlockEntity in renderer's MutableSchema for block {} at pos {}", block.getBlock().getName(), pos);
                } else {
                    getLevel().setBlockEntity(newEntity);
                }
            }
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
