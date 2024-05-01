package com.gregtechceu.gtceu.common.block;

import javax.annotation.Nonnull;

public enum StoneBlockType {
    STONE("stone", "%s"),
    SMOOTH("smooth", "smooth_%s"),
    COBBLE("cobble", "%s_cobblestone", 2.0f, 6.0f),
    COBBLE_MOSSY("cobble_mossy", "mossy_%s_cobblestone", 2.0f, 6.0f),
    POLISHED("polished", "polished_%s"),
    BRICKS("bricks", "%s_bricks"),
    BRICKS_CRACKED("bricks_cracked", "cracked_%s_bricks"),
    BRICKS_MOSSY("bricks_mossy", "mossy_%s_bricks"),
    CHISELED("chiseled", "chiseled_%s"),
    TILED("tiled", "%s_tile"),
    TILED_SMALL("tiled_small", "%s_small_tile"),
    BRICKS_SMALL("bricks_small", "small_%s_bricks"),
    BRICKS_SQUARE("bricks_square", "square_%s_bricks");

    public final String id;
    public final String blockId;
    public final float hardness;
    public final float resistance;

    StoneBlockType(@Nonnull String id, @Nonnull String blockId) {
        this(id, blockId, 1.5f, 6.0f); // vanilla stone stats
    }

    StoneBlockType(@Nonnull String id, @Nonnull String blockId, float hardness, float resistance) {
        this.id = id;
        this.blockId = blockId;
        this.hardness = hardness;
        this.resistance = resistance;
    }
}