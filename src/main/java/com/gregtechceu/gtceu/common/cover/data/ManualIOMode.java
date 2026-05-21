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
        return "cover.manual.mode.title";
    }

    public String getTooltip() {
        return "cover.manual.mode." + localeName;
    }

    public String getDescription() {
        return "cover.manual.mode." + localeName + ".description";
    }
}
