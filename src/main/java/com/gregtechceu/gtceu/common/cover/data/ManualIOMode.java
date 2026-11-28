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

    public static String getTitle() {
        return "cover.gtceu.manual_io_mode.title";
    }

    public String getTooltip() {
        return "cover.gtceu.manual_io_mode." + localeName;
    }

    public String getDescription() {
        return "cover.gtceu.manual_io_mode." + localeName + ".description";
    }
}
