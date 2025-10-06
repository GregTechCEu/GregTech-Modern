package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;

import org.apache.logging.log4j.Level;

import java.util.Objects;

public class GuiError {

    public static void throwNew(IGuiElement guiElement, Type type, String msg) {
        GuiErrorHandler.INSTANCE.pushError(guiElement, type, msg);
    }

    private final Level level = Level.ERROR;
    private final String msg;
    private final IGuiElement reference;
    private final Type type;

    protected GuiError(String msg, IGuiElement reference, Type type) {
        this.msg = msg;
        this.reference = reference;
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + this.level.name() + "][" + this.reference.toString() + "][" + this.type.toString() + "]: " +
                this.msg;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.level, this.reference, this.type);
    }

    public enum Type {
        DRAW,
        SIZING,
        WIDGET_TREE,
        INTERACTION,
        SYNC
    }
}
