package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

public interface IFilterType extends StringRepresentable {

    /**
     * @return The cleanroom type of this filter.
     */
    @Nonnull
    CleanroomType getCleanroomType();

}
