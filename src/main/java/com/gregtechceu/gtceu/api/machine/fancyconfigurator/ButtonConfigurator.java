package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Accessors(chain = true)
public class ButtonConfigurator implements IFancyConfiguratorButton {

    @Getter
    protected IGuiTexture icon;

    protected Consumer<ClickData> onClick;

    @Getter
    @Setter
    protected List<Component> tooltips = Collections.emptyList();

    public ButtonConfigurator(IGuiTexture texture, Consumer<ClickData> onClick) {
        this.icon = texture;
        this.onClick = onClick;
    }

    @Override
    public void onClick(ClickData clickData) {
        onClick.accept(clickData);
    }
}
