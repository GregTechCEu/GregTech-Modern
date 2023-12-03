package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.function.DoubleSupplier;

@LDLRegister(name = "dual_progress", group = "widget.group")
@Accessors(chain = true)
public class DualProgressWidget extends WidgetGroup {
    @Configurable
    @Getter
    ProgressWidget texture1;
    @Configurable
    @Getter
    ProgressWidget texture2;

    public DualProgressWidget() {
        this(() -> Math.abs(System.currentTimeMillis() % 2000) / 2000.0, 0.5);
    }

    public DualProgressWidget(DoubleSupplier progress, double splitPoint) {
        this(new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, 16, 16), new ProgressWidget(ProgressWidget.JEIProgress, 16, 0, 16, 16), progress, splitPoint);
    }

    public DualProgressWidget(ProgressWidget texture1, ProgressWidget texture2, DoubleSupplier progress, double splitPoint) {
        this.texture1 = texture1.setProgressSupplier(() -> progress.getAsDouble() >= splitPoint ? 1.0 : (1.0 / splitPoint) * progress.getAsDouble());
        this.texture2 = texture2.setProgressSupplier(() -> progress.getAsDouble() >= splitPoint ? (1.0 / (1 - splitPoint)) * (progress.getAsDouble() - splitPoint) : 0);
        this.addWidget(this.texture1).addWidget(this.texture2);
    }
}
