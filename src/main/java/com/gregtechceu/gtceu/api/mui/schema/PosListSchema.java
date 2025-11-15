package com.gregtechceu.gtceu.api.mui.schema;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
    private BiPredicate<BlockPos, BlockState> renderFilter = (pos, state) -> true;

    public PosListSchema(Level level, Iterable<? extends BlockPos> posList,
                         BiPredicate<BlockPos, BlockState> renderFilter) {
        this.level = level;
        this.posList = posList;
        if (renderFilter != null) {
            this.renderFilter = renderFilter;
        }
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return new Iterator<>() {

            private final Iterator<? extends BlockPos> posIt = PosListSchema.this.posList.iterator();
            private final MutablePair<BlockPos, BlockState> pair = new MutablePair<>();

            @Override
            public boolean hasNext() {
                return posIt.hasNext();
            }

            @Override
            public Pair<BlockPos, BlockState> next() {
                BlockPos pos = posIt.next();
                pair.setLeft(pos);
                BlockState state = PosListSchema.this.level.getBlockState(pos);
                if (renderFilter == null || renderFilter.test(pos, state)) {
                    pair.setRight(state);
                } else {
                    pair.setRight(Blocks.AIR.defaultBlockState());
                }
                return pair;
            }
        };
    }
}
