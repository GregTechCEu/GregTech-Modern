package com.gregtechceu.gtceu.common.cover.data;

public enum ManualIOMode {

    DISABLED("disabled"),
    FILTERED("filtered"),
    UNFILTERED("unfiltered");

    public static final ManualIOMode[] VALUES = values();

    public final String localeName;

    ManualIOMode(String localeName) {
        this.localeName = localeName;
    }

    public String getTooltip() {
        return "cover.universal.manual_import_export.mode." + localeName;
    }
}
