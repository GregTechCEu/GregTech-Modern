package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum CleanroomCasingType implements StringRepresentable {

    PLASCRETE("plascrete"),
    FILTER_CASING("filter_casing"),
    FILTER_CASING_STERILE("sterilizing_filter_casing");

    private final String name;

    CleanroomCasingType(String name) {
        this.name = name;
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

    public static Block getFromType(CleanroomCasingType casingType) {
        return switch (casingType) {
            case PLASCRETE -> GTBlocks.PLASTCRETE.get();
            case FILTER_CASING -> GTBlocks.FILTER_CASING.get();
            case FILTER_CASING_STERILE -> GTBlocks.FILTER_CASING_STERILE.get();
        };
    }

    @Nullable
    public static CleanroomCasingType getFromBlock(Block block) {
        String blockIdPath = Registry.BLOCK.getKey(block).getPath();
        return switch (blockIdPath) {
            case "plastcrete" -> PLASCRETE;
            case "filter_casing" -> FILTER_CASING;
            case "filter_casing_sterile" -> FILTER_CASING_STERILE;
            default -> null;
        };
    }
}
