package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.utils.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectorWidget extends WidgetGroup {

    public final TextTexture textTexture = new TextTexture("");
    private List<String> candidates = new ArrayList<>();
    private String value = "";
    private Consumer<String> onChanged = ignored -> {};
    private Supplier<String> supplier;
    private Supplier<List<String>> candidatesSupplier;
    private int maxCount = 5;
    private boolean isUp;

    public SelectorWidget() {}

    public SelectorWidget(int x, int y, int width, int height, List<String> candidates, int index) {
        super(x, y, width, height);
        this.candidates = new ArrayList<>(candidates);
        if (index >= 0 && index < candidates.size()) {
            value = candidates.get(index);
        }
    }

    public SelectorWidget setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public SelectorWidget setIsUp(boolean isUp) {
        this.isUp = isUp;
        return this;
    }

    public SelectorWidget setFontColor(int color) {
        textTexture.setColor(color);
        return this;
    }

    public SelectorWidget setValue(String value) {
        this.value = value;
        onChanged.accept(value);
        return this;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = new ArrayList<>(candidates);
    }

    public SelectorWidget setButtonBackground(IGuiTexture... textures) {
        setBackground(textures);
        return this;
    }

    public SelectorWidget setBackground(IGuiTexture texture) {
        super.setBackground(texture);
        return this;
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
    }

    public void setShow(boolean show) {
        setVisible(show);
    }

    public String getValue() {
        return supplier == null ? value : supplier.get();
    }

    public SelectorWidget setCandidatesSupplier(Supplier<List<String>> candidatesSupplier) {
        this.candidatesSupplier = candidatesSupplier;
        return this;
    }

    public SelectorWidget setOnChanged(Consumer<String> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public SelectorWidget setSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public void updateScreen() {
        if (candidatesSupplier != null) {
            candidates = candidatesSupplier.get();
        }
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        textTexture.updateText(getValue());
        textTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY) || candidates.isEmpty()) return false;
        int index = candidates.indexOf(value);
        setValue(candidates.get((index + 1 + candidates.size()) % candidates.size()));
        return true;
    }

    public void addWidgetsConfigurator(ConfiguratorGroup group) {}

    public boolean canWidgetAccepted(IConfigurableWidget widget) {
        return false;
    }
}
