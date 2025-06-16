package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@Accessors(chain = true)
public class ConfirmTextInputWidget extends WidgetGroup {

    private final Consumer<String> textResponder;
    @Nullable
    private final Function<String, String> returnValidator;
    private Function<String, String> validator = (s) -> s;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String inputText = "";
    @Setter
    private String tooltip = "";

    public ConfirmTextInputWidget(int x, int y, int width, int height, String text,
                                  Consumer<String> textResponder,
                                  @Nullable Function<String, String> validator,
                                  @Nullable Function<String, String> returnValidator) {
        super(x, y, width, height);
        this.textResponder = textResponder;
        this.returnValidator = returnValidator;
        if (validator != null) {
            this.validator = validator;
        }
        if (text != null) {
            this.inputText = text;
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.addWidget(new ButtonWidget(
                getSizeWidth() - getSizeHeight(),
                0,
                getSizeHeight(),
                getSizeHeight(),
                pressed -> {
                    if (returnValidator != null) {
                        inputText = returnValidator.apply(inputText);
                    }
                    textResponder.accept(inputText);
                })
                .setButtonTexture(
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, GuiTextures.BUTTON_CHECK)));
        this.addWidget(new TextFieldWidget(
                1,
                1,
                getSizeWidth() - getSizeHeight() - 4,
                getSizeHeight() - 2,
                this::getInputText,
                this::setInputText)
                .setValidator(validator)
                .setHoverTooltips(tooltip));
    }
}
