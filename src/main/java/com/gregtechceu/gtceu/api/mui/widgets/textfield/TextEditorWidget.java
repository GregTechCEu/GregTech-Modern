package com.gregtechceu.gtceu.api.mui.widgets.textfield;

import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.StringValue;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.ValueSyncHandler;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * A syncable, multiline text input widget. Meant to edit large amounts of text.
 */
public class TextEditorWidget extends BaseTextFieldWidget<TextEditorWidget> {

    private IStringValue<?> stringValue;

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
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue<?> iStringValue &&
                syncHandler instanceof ValueSyncHandler<?> valueSyncHandler) {
            this.stringValue = iStringValue;
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
            return true;
        }
        return false;
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

    @NotNull
    public String getText() {
        return this.handler.getText().stream().reduce("", (s1, s2) -> s1 + '\n' + s2).substring(1);
    }

    public void setText(@NotNull String text) {
        this.handler.getText().clear();
        this.handler.getText().addAll(Arrays.stream(text.split("\n")).toList());
    }

    public TextEditorWidget value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        setValue(stringValue);
        return this;
    }

    protected Component processToken(String token) {
        return Component.literal(token);
    }

    @Override
    public List<Component> getTextAsComponents() {
        return this.handler.getText().stream().map(line -> {
            String token = "";
            MutableComponent component = Component.empty();
            for (char c : line.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    if (!token.isEmpty())
                        component.append(processToken(token));
                    token = "";
                    component.append(String.valueOf(c));
                } else token += c;
            }
            if (!token.isEmpty())
                component.append(processToken(token));
            return (Component) component;
        }).toList();
    }
}
