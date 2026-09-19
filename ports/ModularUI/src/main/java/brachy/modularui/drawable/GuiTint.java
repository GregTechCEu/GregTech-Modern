package brachy.modularui.drawable;

/** Extraction-time tint, captured into each submitted primitive instead of changing GPU state. */
public final class GuiTint {
    private static final ThreadLocal<Integer> COLOR = ThreadLocal.withInitial(() -> 0xFFFFFFFF);

    private GuiTint() {}

    public static int get() {
        return COLOR.get();
    }

    public static void set(int argb) {
        COLOR.set(argb);
    }
}
