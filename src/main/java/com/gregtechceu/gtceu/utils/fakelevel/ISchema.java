package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockInfo>> {

    Level getLevel();

    Vec3 getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter);

    @Nullable
    BiPredicate<BlockPos, BlockInfo> getRenderFilter();
}
