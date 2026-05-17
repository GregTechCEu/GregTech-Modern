package com.gregtechceu.gtceu.common.cover.data;

public enum DistributionMode {

    ROUND_ROBIN_GLOBAL("round_robin_global"),
    ROUND_ROBIN_PRIO("round_robin_prio"),
    INSERT_FIRST("insert_first");

    public static final DistributionMode[] VALUES = values();
    private static final float OFFSET = 1.0f / VALUES.length;

    public final String localeName;

    DistributionMode(String localeName) {
        this.localeName = localeName;
    }
}
