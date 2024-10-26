package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.widget.directional.CombinedDirectionalConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.directional.IDirectionalConfigHandler;
import com.gregtechceu.gtceu.api.gui.widget.directional.handlers.AutoOutputFluidConfigHandler;
import com.gregtechceu.gtceu.api.gui.widget.directional.handlers.AutoOutputItemConfigHandler;
import com.gregtechceu.gtceu.api.gui.widget.directional.handlers.CoverableConfigHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CombinedDirectionalFancyConfigurator implements IFancyUIProvider {

    private final List<Supplier<IDirectionalConfigHandler>> configs;
    private final MetaMachine machine;

    public CombinedDirectionalFancyConfigurator(List<Supplier<IDirectionalConfigHandler>> configs,
                                                MetaMachine machine) {
        this.configs = configs;
        this.machine = machine;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        Size parentSize = widget.getSize();
        return new CombinedDirectionalConfigurator(
                widget, configs.stream().map(Supplier::get).toArray(IDirectionalConfigHandler[]::new), machine,
                parentSize.width - 8,
                parentSize.height - WidgetUtils.getInventoryHeight(true));
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.TOOL_COVER_SETTINGS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.directional_setting.title"); // TODO add this
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<Function<MetaMachine, Supplier<IDirectionalConfigHandler>>> CONFIG_HANDLERS = new ArrayList<>();

    static {
        // Left side:
        CONFIG_HANDLERS.add(
                machine -> machine instanceof IAutoOutputItem autoOutputItem && autoOutputItem.hasAutoOutputItem() ?
                        () -> new AutoOutputItemConfigHandler(autoOutputItem) : null);
        CONFIG_HANDLERS.add(
                machine -> machine instanceof IAutoOutputFluid autoOutputFluid && autoOutputFluid.hasAutoOutputFluid() ?
                        () -> new AutoOutputFluidConfigHandler(autoOutputFluid) : null);

        // Right side:
        CONFIG_HANDLERS.add(machine -> () -> new CoverableConfigHandler(machine.getCoverContainer()));
    }

    /**
     * To be used by addons for registering their own directional configurations
     */
    public static void registerConfigHandler(Function<MetaMachine, Supplier<IDirectionalConfigHandler>> factory) {
        CONFIG_HANDLERS.add(factory);
    }

    @Nullable
    public static CombinedDirectionalFancyConfigurator of(MetaMachine container, MetaMachine machine) {
        var configs = CONFIG_HANDLERS.stream()
                .map(handler -> handler.apply(container))
                .filter(Objects::nonNull)
                .toList();

        return configs.isEmpty() ? null : new CombinedDirectionalFancyConfigurator(configs, machine);
    }
}
