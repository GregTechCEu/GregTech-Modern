package brachy.modularui.drawable.schema;

import brachy.modularui.drawable.SchemaRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import java.util.Map;
import java.util.function.BiConsumer;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockState>> {

    Level getLevel();

    Vector3fc getFocus();

    BlockPos getOrigin();

    default SchemaRenderer createRenderer() {
        return new SchemaRenderer(this);
    }

    default void forEach(@NotNull BiConsumer<BlockPos, BlockState> action) {
        for (var entry : this) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }
}
