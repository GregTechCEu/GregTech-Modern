package com.gregtechceu.gtceu.common.cover.data;

import lombok.Getter;

public enum VoidingMode {

    VOID_ANY("cover.voiding.voiding_mode.void_any", "void_any", 1),
    VOID_OVERFLOW("cover.voiding.voiding_mode.void_overflow", "void_overflow", 1024);

    @Getter
    public final String tooltip;
    public final int maxStackSize;

    VoidingMode(String tooltip, String textureName, int maxStackSize) {
        this.tooltip = tooltip;
        this.maxStackSize = maxStackSize;
    }
}
