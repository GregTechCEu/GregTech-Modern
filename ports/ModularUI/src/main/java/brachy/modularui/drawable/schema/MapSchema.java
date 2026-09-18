package brachy.modularui.drawable.schema;

import brachy.modularui.utils.BlockPosUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MapSchema implements ISchema {

    @Getter
    private final Level level = new SchemaLevel();
    private final Long2ReferenceOpenHashMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
    @Getter
    private final BlockPos origin;
    private final Vector3f center;

    public MapSchema(Map<BlockPos, BlockState> blocks) {
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
        this.center = BlockPosUtil.getCenterF(min, max);
    }

    public MapSchema(Long2ReferenceMap<BlockState> blocks) {
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
        this.center = BlockPosUtil.getCenterF(min, max);
    }

    @Override
    public Vector3fc getFocus() {
        return center;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return new AbstractIterator<>() {

            private final ObjectIterator<Long2ReferenceMap.Entry<BlockState>> it = blocks.long2ReferenceEntrySet().fastIterator();

            @Override
            protected Map.Entry<BlockPos, BlockState> computeNext() {
                if (it.hasNext()) {
                    Long2ReferenceMap.Entry<BlockState> entry = it.next();
                    BlockPos key = BlockPos.of(entry.getLongKey());
                    return Map.entry(key, entry.getValue());
                }
                return endOfData();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapSchema entries)) return false;

        return Objects.equals(level, entries.level) && blocks.equals(entries.blocks) &&
                Objects.equals(origin, entries.origin) && Objects.equals(center, entries.center);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(level);
        result = 31 * result + blocks.hashCode();
        result = 31 * result + Objects.hashCode(origin);
        result = 31 * result + Objects.hashCode(center);
        return result;
    }

    @Accessors(chain = true)
    public static class Builder {

        private final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();

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
            return new MapSchema(this.blocks);
        }
    }
}
