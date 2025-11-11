package com.gregtechceu.gtceu.api.mui.widgets.textfield;

/**
 * A non syncable, multiline text input widget. Meant for client only screens to edit large amounts of text.
 */
// TODO steal from Mclib
// ^ what? this is from 1.12, I guess I'll leave it here?
public class TextEditorWidget extends BaseTextFieldWidget<TextEditorWidget> {

    public TextEditorWidget() {
        this.handler.setMaxLines(10000);
    }
}
