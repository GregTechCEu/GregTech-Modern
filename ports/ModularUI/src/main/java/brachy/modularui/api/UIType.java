package brachy.modularui.api;

public enum UIType {

    MODULAR_SCREEN(true),
    EMBED(false),
    NONE(false);

    public final boolean isScreen;

    UIType(boolean isScreen) {
        this.isScreen = isScreen;
    }
}
