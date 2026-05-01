package com.lowdragmc.lowdraglib.gui.widget.codeeditor;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CodeEditorWidget extends WidgetGroup {

    public static final Identifier MONO_BOLD = Identifier.fromNamespaceAndPath("ldlib", "mono_bold");
    public final CodeEditor codeEditor = new CodeEditor();
    private List<String> lines = new ArrayList<>();
    private Consumer<List<String>> onTextChanged = ignored -> {};

    public CodeEditorWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public boolean canConsumeInput() {
        return isFocus();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
    }

    public void notifyChanged() {
        onTextChanged.accept(lines);
    }

    public Cursor getCursor(double mouseX, double mouseY) {
        return new Cursor(0, 0);
    }

    public void adaptCursor() {}

    public CodeEditor getCodeEditor() {
        return codeEditor;
    }

    public int getScrollXOffset() {
        return 0;
    }

    public int getScrollYOffset() {
        return 0;
    }

    public Consumer<List<String>> getOnTextChanged() {
        return onTextChanged;
    }

    public void setOnTextChanged(Consumer<List<String>> onTextChanged) {
        this.onTextChanged = onTextChanged;
    }
}
