package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.gui.editor.configurator.Configurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.function.DoubleSupplier;

@LDLRegister(name = "dual_progress", group = "widget.group")
@Accessors(chain = true)
public class DualProgressWidget extends WidgetGroup {

    private DoubleSupplier progressSupplier;
    @Configurable
    @NumberRange(range = { 0.001, 1.0 }, wheel = 0.01)
    private double splitPoint;
    @Configurable(subConfigurable = true)
    @Getter
    private ProgressWidget texture1;
    @Configurable(subConfigurable = true)
    @Getter
    private ProgressWidget texture2;

    public DualProgressWidget() {
        this(ProgressWidget.JEIProgress, 0.5);
    }

    public DualProgressWidget(DoubleSupplier progress, double splitPoint) {
        this(new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, 16, 16),
                new ProgressWidget(ProgressWidget.JEIProgress, 16, 0, 16, 16), progress, splitPoint);
    }

    public DualProgressWidget(ProgressWidget texture1, ProgressWidget texture2, DoubleSupplier progress,
                              double splitPoint) {
        this.progressSupplier = progress;
        this.splitPoint = splitPoint;
        this.texture1 = texture1.setProgressSupplier(
                () -> progress.getAsDouble() >= splitPoint ? 1.0 : (1.0 / splitPoint) * progress.getAsDouble());
        this.texture2 = texture2.setProgressSupplier(() -> progress.getAsDouble() >= splitPoint ?
                (1.0 / (1 - splitPoint)) * (progress.getAsDouble() - splitPoint) : 0);
        this.addWidget(this.texture1).addWidget(this.texture2);
    }

    public DualProgressWidget setTexture1(ProgressWidget widget) {
        this.removeWidget(texture1);
        this.texture1 = widget.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ? 1.0 :
                (1.0 / splitPoint) * progressSupplier.getAsDouble());
        this.addWidget(texture1);
        return this;
    }

    public DualProgressWidget setTexture2(ProgressWidget widget) {
        this.removeWidget(texture2);
        this.texture2 = widget.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ?
                (1.0 / (1 - splitPoint)) * (progressSupplier.getAsDouble() - splitPoint) : 0);
        this.addWidget(texture2);
        return this;
    }

    public DualProgressWidget setProgressSupplier(DoubleSupplier progressSupplier) {
        this.progressSupplier = progressSupplier;

        this.widgets.clear();
        this.texture1.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ? 1.0 :
                (1.0 / splitPoint) * progressSupplier.getAsDouble());
        this.addWidget(texture1);
        this.texture2.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ?
                (1.0 / (1 - splitPoint)) * (progressSupplier.getAsDouble() - splitPoint) : 0);
        this.addWidget(texture2);

        return this;
    }

    public DualProgressWidget setSplitPoint(double splitPoint) {
        this.splitPoint = splitPoint;

        this.widgets.clear();
        this.texture1.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ? 1.0 :
                (1.0 / splitPoint) * progressSupplier.getAsDouble());
        this.addWidget(texture1);
        this.texture2.setProgressSupplier(() -> progressSupplier.getAsDouble() >= splitPoint ?
                (1.0 / (1 - splitPoint)) * (progressSupplier.getAsDouble() - splitPoint) : 0);
        this.addWidget(texture2);

        return this;
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        super.buildConfigurator(father);
        for (Configurator configurator : father.getConfigurators()) {
            if (!setConfiguratorIfProgress(configurator) && configurator instanceof ConfiguratorGroup group) {
                for (Configurator subConfigurator : group.getConfigurators()) {
                    setConfiguratorIfProgress(subConfigurator);
                }
            }
        }
    }

    private boolean setConfiguratorIfProgress(Configurator configurator) {
        if (configurator instanceof WrapperConfigurator guiConfigurator &&
                guiConfigurator.inner instanceof ProgressWidget progressWidget) {
            if (configurator.getName().equals("texture1")) {
                this.setTexture1(progressWidget);
                return true;
            } else if (configurator.getName().equals("texture2")) {
                this.setTexture2(progressWidget);
                return true;
            }
        }
        return false;
    }
}
