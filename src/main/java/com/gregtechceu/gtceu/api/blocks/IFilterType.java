package com.gregtechceu.gtceu.api.blocks;

import com.gregtechceu.gtceu.api.machines.multiblock.CleanroomType;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public interface IFilterType extends StringRepresentable {

    /**
     * @return The cleanroom type of this filter.
     */
    @NotNull
    CleanroomType getCleanroomType();

}
