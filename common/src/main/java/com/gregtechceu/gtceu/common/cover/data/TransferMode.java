package com.gregtechceu.gtceu.common.cover.data;

import java.util.Arrays;
import java.util.Comparator;

public enum TransferMode {
    TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", 1),
    TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", 1024),
    KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", 1024);

    public final String localeName;
    public final int maxStackSize;

    TransferMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }
}
