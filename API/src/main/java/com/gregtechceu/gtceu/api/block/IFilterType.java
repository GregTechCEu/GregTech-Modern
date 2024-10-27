package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public interface IFilterType extends StringRepresentable {

    /**
     * @return The cleanroom type of this filter.
     */
    @NotNull
    CleanroomType getCleanroomType();
}
