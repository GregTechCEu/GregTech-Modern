package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToggleConfigurator extends ButtonConfigurator {

    private final IGuiTexture enabledTexture;
    private final IGuiTexture disabledTexture;
    private final Consumer<ClickData> onToggle;
    private final Supplier<Boolean> isEnabled;

    public ToggleConfigurator(
            IGuiTexture enabledTexture,
            IGuiTexture disabledTexture,
            Supplier<Boolean> isEnabled,
            Consumer<ClickData> onToggle
    ) {
        super(isEnabled.get() ? enabledTexture : disabledTexture, (cd) -> {});

        this.enabledTexture = enabledTexture;
        this.disabledTexture = disabledTexture;

        this.isEnabled = isEnabled;
        this.onToggle = onToggle;
    }

    @Override
    public void onClick(ClickData clickData) {
        this.onToggle.accept(clickData);

        this.icon = this.isEnabled.get() ? this.enabledTexture : this.disabledTexture;
    }
}
