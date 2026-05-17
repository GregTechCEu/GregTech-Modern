package com.gregtechceu.gtceu.common.cover.data;

import lombok.Getter;

public enum TransferMode {

    TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", "transfer_any", 1),
    TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", "transfer_exact", 1024),
    KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", "keep_exact", 1024);

    @Getter
    public final String tooltip;
    public final int maxStackSize;

    TransferMode(String tooltip, String textureName, int maxStackSize) {
        this.tooltip = tooltip;
        this.maxStackSize = maxStackSize;
    }
}
