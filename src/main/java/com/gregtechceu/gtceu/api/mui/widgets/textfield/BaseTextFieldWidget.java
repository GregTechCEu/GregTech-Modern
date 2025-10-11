package com.gregtechceu.gtceu.api.mui.widgets.textfield;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.widget.IFocusedWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Stencil;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTextFieldTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.AbstractScrollWidget;
import com.gregtechceu.gtceu.api.mui.widget.scroll.HorizontalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollData;
import com.gregtechceu.gtceu.api.mui.widgets.VoidWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of a text input widget. Handles mouse/InputConstants input and rendering.
 */
public class BaseTextFieldWidget<W extends BaseTextFieldWidget<W>> extends AbstractScrollWidget<VoidWidget, W>
                                implements IFocusedWidget {

    public static final DecimalFormat format = new DecimalFormat("###.###");

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");
    public static final Pattern DECIMALS = Pattern.compile(
            "[0-9]*(" + getDecimalSeparator() + "[0-9]*)?([+\\-*/%^][0-9]*(" + getDecimalSeparator() + "[0-9]*)?)*");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[^§]");

    private static final int CURSOR_BLINK_RATE = 10;
    // max time between clicks to count as double-click, in ms
    private static final int DOUBLE_CLICK_THRESHOLD = 300;

    protected TextFieldHandler handler = new TextFieldHandler(this);
    protected TextFieldRenderer renderer = new TextFieldRenderer(this.handler);
    protected Alignment textAlignment = Alignment.CenterLeft;
    @Getter
    protected List<String> lastText;
    protected int scrollOffset = 0;
    protected float scale = 1f;
    protected boolean focusOnGuiOpen;
    private int cursorTimer;
    protected long lastClickTime = 0;

    protected Integer textColor;
    protected Integer markedColor;
    protected Component hintText = null;
    protected Integer hintTextColor;

    public BaseTextFieldWidget() {
        super(new HorizontalScrollData(), null);
        this.handler.setRenderer(this.renderer);
        this.handler.setScrollArea(getScrollArea());
        padding(4, 0);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean isChildValid(VoidWidget child) {
        return false;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.handler.setGuiContext(getContext());
    }

    @Override
    public void afterInit() {
        super.afterInit();
        if (this.focusOnGuiOpen) {
            getContext().focus(this);
            this.handler.markAll();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isFocused() && ++this.cursorTimer == CURSOR_BLINK_RATE) {
            this.renderer.toggleCursor();
            this.cursorTimer = 0;
        }
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (transformed) {
            WidgetTextFieldTheme widgetTheme = (WidgetTextFieldTheme) getWidgetTheme(context.getTheme());
            this.renderer.setColor(this.textColor != null ? this.textColor : widgetTheme.getTextColor());
            this.renderer.setCursorColor(this.textColor != null ? this.textColor : widgetTheme.getTextColor());
            this.renderer.setMarkedColor(this.markedColor != null ? this.markedColor : widgetTheme.getMarkedColor());
            setupDrawText(context, widgetTheme);
            drawText(context, widgetTheme);
        } else {
            Stencil.apply(1, 1, getArea().w() - 2, getArea().h() - 2, context);
        }
    }

    protected void setupDrawText(ModularGuiContext context, WidgetTextFieldTheme widgetTheme) {
        this.renderer.setSimulate(false);
        this.renderer.setScale(this.scale);
        this.renderer.setAlignment(this.textAlignment, -2, getArea().height);
    }

    protected void drawText(ModularGuiContext context, WidgetTextFieldTheme widgetTheme) {
        if (this.handler.isTextEmpty() && this.hintText != null) {
            int c = this.renderer.getColor();
            int hintColor = this.hintTextColor != null ? this.hintTextColor : widgetTheme.getHintColor();
            this.renderer.setColor(hintColor);
            this.renderer.draw(context.getGraphics(), Collections.singletonList(this.hintText));
            this.renderer.setColor(c);
        } else {
            this.renderer.draw(context.getGraphics(), this.handler.getTextAsComponents());
        }
        getScrollArea().getScrollX().setScrollSize(Math.max(0, (int) (this.renderer.getLastWidth() + 0.5f)));
    }

    @Override
    public WidgetTextFieldTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getTextFieldTheme();
    }

    @Override
    public boolean isFocused() {
        return getContext().isFocused(this);
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
        this.lastText = new ArrayList<>(this.handler.getText());
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        this.renderer.setCursor(false);
        this.cursorTimer = 0;
        this.scrollOffset = 0;
        this.handler.setCursor(0, 0, true, true);
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        Result result = super.onMousePressed(mouseX, mouseY, button);
        if (result != Result.IGNORE) {
            return Result.SUCCESS; // keep focused
        }
        if (!isHovering()) {
            return Result.IGNORE;
        }
        if (button == 1) {
            this.handler.clear();
        } else {
            // the current transformation does not include the transformation of the children (the scroll) so we need to
            // manually transform here
            int x = getContext().getMouseX() + getScrollX();
            int y = getContext().getMouseY() + getScrollY();
            long now = Util.getMillis();
            if (this.lastClickTime < 0) {
                // triple click
                if (now + this.lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                    this.handler.markAll();
                    this.lastClickTime = now;
                    return Result.SUCCESS;
                }
                this.lastClickTime = 0;
            } else if (this.lastClickTime > 0) {
                // double click
                if (now - this.lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                    this.handler.markCurrentLine();
                    this.lastClickTime = -Util.getMillis();
                    return Result.SUCCESS;
                }
                this.lastClickTime = 0;
            }
            this.handler.setCursor(this.renderer.getCursorPos(this.handler.getText(), x, y), true);
            this.lastClickTime = Util.getMillis();
        }
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isFocused() && !getScrollArea().isDragging()) {
            int x = getContext().getMouseX() + getScrollX();
            int y = getContext().getMouseY() + getScrollY();
            this.handler.setMainCursor(this.renderer.getCursorPos(this.handler.getText(), x, y), true);
        }
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) {
            return Result.IGNORE;
        }
        switch (keyCode) {
            case InputConstants.KEY_NUMPADENTER:
            case InputConstants.KEY_RETURN:
                if (getMaxLines() > 1) {
                    this.handler.newLine();
                } else {
                    getContext().removeFocus();
                }
                return Result.SUCCESS;
            case InputConstants.KEY_ESCAPE:
                if (ConfigHolder.INSTANCE.client.ui.escRestoreLastText) {
                    this.handler.clear();
                    this.handler.insert(this.lastText);
                }
                getContext().removeFocus();
                return Result.SUCCESS;
            case InputConstants.KEY_LEFT: {
                this.handler.moveCursorLeft((modifiers & GLFW.GLFW_MOD_CONTROL) != 0,
                        (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
                return Result.SUCCESS;
            }
            case InputConstants.KEY_RIGHT: {
                this.handler.moveCursorRight((modifiers & GLFW.GLFW_MOD_CONTROL) != 0,
                        (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
                return Result.SUCCESS;
            }
            case InputConstants.KEY_UP: {
                this.handler.moveCursorUp((modifiers & GLFW.GLFW_MOD_CONTROL) != 0,
                        (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
                return Result.SUCCESS;
            }
            case InputConstants.KEY_DOWN: {
                this.handler.moveCursorDown((modifiers & GLFW.GLFW_MOD_CONTROL) != 0,
                        (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
                return Result.SUCCESS;
            }
            case InputConstants.KEY_DELETE:
                this.handler.delete(true);
                return Result.SUCCESS;
            case InputConstants.KEY_BACKSPACE:
                this.handler.delete();
                return Result.SUCCESS;
        }

        if (keyCode == Character.MIN_VALUE) {
            return Result.STOP;
        }

        if (Screen.isCopy(keyCode)) {
            // copy marked text
            Minecraft.getInstance().keyboardHandler.setClipboard(this.handler.getSelectedText());
            return Result.SUCCESS;
        } else if (Screen.isPaste(keyCode)) {
            if (this.handler.hasTextMarked()) {
                this.handler.delete();
            }
            // paste copied text in marked text
            this.handler.insert(Minecraft.getInstance().keyboardHandler.getClipboard().replace("§", ""));
            return Result.SUCCESS;
        } else if (Screen.isCut(keyCode) && this.handler.hasTextMarked()) {
            // copy and delete copied text
            Minecraft.getInstance().keyboardHandler.setClipboard(this.handler.getSelectedText());
            this.handler.delete();
            return Result.SUCCESS;
        } else if (Screen.isSelectAll(keyCode)) {
            // mark whole text
            this.handler.markAll();
            return Result.SUCCESS;
        } else if (BASE_PATTERN.matcher(String.valueOf((char) keyCode)).matches() &&
                handler.test(String.valueOf(keyCode))) {
                    if (this.handler.hasTextMarked()) {
                        this.handler.delete();
                    }
                    // insert typed char
                    this.handler.insert(String.valueOf((char) keyCode));
                    return Result.SUCCESS;
                }

        return Result.STOP;
    }

    @Override
    public @NotNull Result onCharTyped(char codePoint, int modifiers) {
        if (!isFocused()) {
            return Result.IGNORE;
        }
        if (codePoint == Character.MIN_VALUE) {
            return Result.STOP;
        }
        if (BASE_PATTERN.matcher(String.valueOf(codePoint)).matches() && handler.test(String.valueOf(codePoint))) {
            if (this.handler.hasTextMarked()) {
                this.handler.delete();
            }
            // insert typed char
            this.handler.insert(String.valueOf(codePoint));
            return Result.SUCCESS;
        }
        return Result.STOP;
    }

    public int getMaxLines() {
        return this.handler.getMaxLines();
    }

    public ScrollData getScrollData() {
        return getScrollArea().getScrollX();
    }

    public W setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return getThis();
    }

    public W setScale(float scale) {
        this.scale = scale;
        return getThis();
    }

    public W setTextColor(int color) {
        this.textColor = color;
        return getThis();
    }

    public W setMarkedColor(int color) {
        this.markedColor = color;
        return getThis();
    }

    public W setFocusOnGuiOpen(boolean focusOnGuiOpen) {
        this.focusOnGuiOpen = focusOnGuiOpen;
        return getThis();
    }

    /**
     * Sets a constant hint text. The hint is displayed in a less noticeable color when the field is empty.
     * The color is by default obtained from the current them, but can be overridden with {@link #hintColor(int)}.
     *
     * @param hint hint text to display
     * @return this
     */
    public W hintText(Component hint) {
        this.hintText = hint;
        return getThis();
    }

    public W hintColor(int color) {
        this.hintTextColor = color;
        return getThis();
    }

    public static char getDecimalSeparator() {
        return format.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public static char getGroupSeparator() {
        return format.getDecimalFormatSymbols().getGroupingSeparator();
    }
}
