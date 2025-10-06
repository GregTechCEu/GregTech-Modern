package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public abstract class PosListSchema implements ISchema {

    @Getter
    private final Level level;
    private final Iterable<? extends BlockPos> posList;
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockInfo> renderFilter;

    public PosListSchema(Level level, Iterable<? extends BlockPos> posList,
                         BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.level = level;
        this.posList = posList;
        this.renderFilter = renderFilter;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new Iterator<>() {

            private final Iterator<? extends BlockPos> posIt = PosListSchema.this.posList.iterator();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>();

            @Override
            public boolean hasNext() {
                return posIt.hasNext();
            }

            @Override
            public Pair<BlockPos, BlockInfo> next() {
                BlockPos pos = posIt.next();
                pair.setLeft(pos);
                BlockInfo.Mutable.SHARED.set(PosListSchema.this.level, pos);
                if (renderFilter == null || renderFilter.test(pos, BlockInfo.Mutable.SHARED)) {
                    pair.setRight(BlockInfo.Mutable.SHARED);
                } else {
                    pair.setRight(BlockInfo.EMPTY);
                }
                return pair;
            }
        };
    }
}
