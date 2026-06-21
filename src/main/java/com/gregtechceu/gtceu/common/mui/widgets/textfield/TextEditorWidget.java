package com.gregtechceu.gtceu.common.mui.widgets.textfield;

import brachy.modularui.api.value.IStringValue;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.StringValue;
import brachy.modularui.widgets.textfield.BaseTextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A syncable, multiline text input widget. Meant to edit large amounts of text.
 */
public class TextEditorWidget<W extends TextEditorWidget<W>> extends BaseTextFieldWidget<W> {

    protected IStringValue<?> stringValue;

    public TextEditorWidget() {
        this.handler.setMaxLines(10000);
        this.setTextAlignment(Alignment.TopLeft);
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
        setText(this.stringValue.getStringValue());
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        this.stringValue.setStringValue(getText());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = this.stringValue.getStringValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public @NotNull Result onCharTyped(char codePoint, int modifiers) {
        if (codePoint == '\t') {
            if (this.handler.hasTextMarked()) {
                int startY = this.handler.getStartCursor().y;
                int endY = this.handler.getEndCursor().y;
                for (int y = startY; y < endY; y++) {
                    this.handler.getText().set(y, "    " + this.handler.getText().get(y));
                }
            } else {
                int y = this.handler.getMainCursor().y;
                this.handler.getText().set(y, "    " + this.handler.getText().get(y));
            }
        }
        return super.onCharTyped(codePoint, modifiers);
    }

    @NotNull
    public String getText() {
        return this.handler.getText().stream().reduce((s1, s2) -> s1 + '\n' + s2).orElse("");
    }

    public void setText(@NotNull String text) {
        this.handler.getText().clear();
        this.handler.getText().addAll(Arrays.stream(text.split("\n")).toList());
    }

    public W value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        super.setSyncOrValue(stringValue);
        return getThis();
    }
}
