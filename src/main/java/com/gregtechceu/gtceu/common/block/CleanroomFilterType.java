package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import lombok.Getter;

import javax.annotation.Nonnull;

public enum CleanroomFilterType implements IFilterType {

    FILTER_CASING("filter_casing", CleanroomType.CLEANROOM),
    FILTER_CASING_STERILE("sterilizing_filter_casing", CleanroomType.STERILE_CLEANROOM);

    private final String name;
    @Getter
    private final CleanroomType cleanroomType;

    CleanroomFilterType(String name, CleanroomType cleanroomType) {
        this.name = name;
        this.cleanroomType = cleanroomType;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Nonnull
    @Override
    public String toString() {
        return getSerializedName();
    }

}
