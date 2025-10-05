package com.gregtechceu.gtceu.utils.fakelevel;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class SchemaLevel extends DummyLevel implements ISchema {

    private final ObjectLinkedOpenHashSet<BlockPos> blocks = new ObjectLinkedOpenHashSet<>();
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();

    public SchemaLevel() {
        this((blockPos, blockInfo) -> true);
    }

    public SchemaLevel(BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public Level getLevel() {
        return this;
    }

    @Override
    public Vec3 getFocus() {
        return BlockPosUtil.getCenterD(this.min, this.max);
    }

    @Override
    public BlockPos getOrigin() {
        return min;
    }

    @Override
    public boolean setBlock(@NotNull BlockPos pos, @NotNull BlockState newState, int flags) {
        boolean renderTest;
        boolean state;
        if (renderFilter == null || renderFilter.test(pos, BlockInfo.of(this, pos))) {
            renderTest = true;
            state = super.setBlock(pos, newState, flags);
        } else {
            renderTest = state = false;
        }

        if (newState.isAir()) {
            if (this.blocks.remove(pos) && BlockPosUtil.isOnBorder(min, max, pos)) {
                if (this.blocks.isEmpty()) {
                    this.min.set(0, 0, 0);
                    this.max.set(0, 0, 0);
                } else {
                    min.set(BlockPosUtil.MAX);
                    max.set(BlockPosUtil.MIN);
                    for (BlockPos pos1 : blocks) {
                        BlockPosUtil.setMin(min, pos1);
                        BlockPosUtil.setMax(max, pos1);
                    }
                }
            }
        } else if (this.blocks.isEmpty()) {
            if (!renderTest) return false;
            this.blocks.add(pos);
            this.min.set(pos);
            this.max.set(pos);
        } else if (renderTest && this.blocks.add(pos)) {
            BlockPosUtil.setMin(this.min, pos);
            BlockPosUtil.setMax(this.max, pos);
        }
        return renderTest && state;
    }

    @Override
    public @NotNull Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final ObjectListIterator<BlockPos> it = blocks.iterator();
            private final BlockInfo.Mutable info = new BlockInfo.Mutable();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(null, this.info);

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                while (it.hasNext()) {
                    var pos = it.next();
                    this.info.set(SchemaLevel.this, pos);
                    this.pair.setLeft(pos);
                    if (renderFilter == null || renderFilter.test(pos, info)) {
                        return this.pair;
                    }
                }
                return endOfData();
            }
        };
    }
}
