package brachy.modularui.drawable.schema;

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
import java.util.Objects;
import java.util.function.BiPredicate;

public abstract class PosListSchema implements ISchema {

    @Getter
    private final Level level;
    private final Iterable<? extends BlockPos> posList;

    public PosListSchema(Level level, Iterable<? extends BlockPos> posList) {
        this.level = level;
        this.posList = posList;
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
                pair.setRight(state);
                return pair;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PosListSchema entries)) return false;

        return level.equals(entries.level) && posList.equals(entries.posList);
    }

    @Override
    public int hashCode() {
        int result = level.hashCode();
        result = 31 * result + posList.hashCode();
        return result;
    }
}
