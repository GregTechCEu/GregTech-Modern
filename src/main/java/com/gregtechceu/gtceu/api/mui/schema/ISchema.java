package com.gregtechceu.gtceu.api.mui.schema;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockState>> {

    Level getLevel();

    Vec3 getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@NotNull BiPredicate<BlockPos, BlockState> renderFilter);

    @NotNull
    BiPredicate<BlockPos, BlockState> getRenderFilter();

    default void forEach(@NotNull BiConsumer<BlockPos, BlockState> action) {
        for (var entry : this) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }
}
