package com.gregtechceu.gtceu.common.cover.data;

import lombok.Getter;

public enum TransferMode {

    TRANSFER_ANY("transfer_any", 1),
    TRANSFER_EXACT("transfer_exact", 1024),
    KEEP_EXACT("keep_exact", 1024);

    public static final TransferMode[] VALUES = values();

    @Getter
    public final String localeName;
    public final int maxStackSize;

    TransferMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }

    public static String getTitle() {
        return "cover.robotic_arm.transfer_mode.title";
    }

    public String getTooltip() {
        return "cover.robotic_arm.transfer_mode." + localeName;
    }
}
