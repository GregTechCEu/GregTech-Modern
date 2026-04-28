package com.gregtechceu.gtceu.client.model.compat;

public enum TriState {

    DEFAULT,
    TRUE,
    FALSE;

    public net.minecraft.util.TriState toMinecraft() {
        return switch (this) {
            case TRUE -> net.minecraft.util.TriState.TRUE;
            case FALSE -> net.minecraft.util.TriState.FALSE;
            case DEFAULT -> net.minecraft.util.TriState.DEFAULT;
        };
    }
}
