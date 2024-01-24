package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.widget.AutoOutputConfigurator;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/30
 * @implNote AutoOutputConfigurator
 */
public class AutoOutputFancyConfigurator implements IFancyUIProvider {
    final MetaMachine machine;

    public AutoOutputFancyConfigurator(MetaMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.output_setting.title");
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.TOOL_AUTO_OUTPUT;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new AutoOutputConfigurator(machine);
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(getTitle(),
                Component.translatable("gtceu.gui.output_setting.tooltips.0"),
                Component.translatable("gtceu.gui.output_setting.tooltips.1"));
    }
}
