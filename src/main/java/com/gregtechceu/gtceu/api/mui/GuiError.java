package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import lombok.Getter;
import org.apache.logging.log4j.Level;

import java.util.Objects;

public class GuiError {

    public static void throwNew(IWidget guiElement, Type type, String msg) {
        if (GTCEu.isClientSide()) {
            GuiErrorHandler.INSTANCE.pushError(guiElement, type, msg);
        }
    }

    @Getter
    private final Level level = Level.ERROR;
    @Getter
    private final String msg;
    @Getter
    private final IWidget reference;
    @Getter
    private final Type type;

    protected GuiError(String msg, IWidget reference, Type type) {
        this.msg = msg;
        this.reference = reference;
        this.type = type;
    }

    @Override
    public String toString() {
        return "MUI [" + this.type.toString() + "][" + this.reference.toString() + "]: " + this.msg;
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
