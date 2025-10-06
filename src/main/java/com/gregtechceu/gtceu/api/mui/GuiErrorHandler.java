package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class GuiErrorHandler {

    public static final GuiErrorHandler INSTANCE = new GuiErrorHandler();

    private final Set<GuiError> errorSet = new ObjectOpenHashSet<>();
    @Getter
    private final List<GuiError> errors = new ArrayList<>();

    private GuiErrorHandler() {}

    public void clear() {
        this.errors.clear();
    }

    void pushError(IGuiElement reference, GuiError.Type type, String msg) {
        GuiError error = new GuiError(msg, reference, type);
        if (this.errorSet.add(error)) {
            GTCEu.LOGGER.error(msg);
            this.errors.add(error);
        }
    }

    public void drawErrors(int x, int y) {}
}
