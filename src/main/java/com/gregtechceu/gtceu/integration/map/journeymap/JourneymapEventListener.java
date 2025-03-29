package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import journeymap.client.api.display.IThemeButton;
import journeymap.client.api.event.forge.FullscreenDisplayEvent;
import journeymap.client.io.ThemeLoader;

import java.util.ArrayList;
import java.util.Arrays;

public class JourneymapEventListener {

    public static void init() {
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(JourneymapEventListener::onFullscreenAddonButton);
        bus.addListener(JourneymapEventListener::onFullscreenToolbarEvent);
    }

    @OnlyIn(Dist.CLIENT)
    protected static void onFullscreenAddonButton(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) {
            return;
        }
        if (ConfigHolder.INSTANCE.compat.minimap.direction !=
                ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL) {
            return;
        }
        var display = event.getThemeButtonDisplay();
        var buttons = new ArrayList<IThemeButton>(ButtonState.getAllButtons().size());
        for (var state : ButtonState.getAllButtons()) {
            buttons.add(display.addThemeToggleButton("gtceu.button." + state.name, state.name, state.enabled,
                    b -> {
                        ButtonState.toggleButton(state);
                        buttons.stream().filter(btn -> btn.getToggled() || btn == b).forEach(IThemeButton::toggle);
                    }));
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static void onFullscreenToolbarEvent(FullscreenDisplayEvent.CustomToolbarEvent event) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) {
            return;
        }
        if (ConfigHolder.INSTANCE.compat.minimap.direction ==
                ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL) {
            return;
        }
        var builder = event.getCustomToolBarBuilder();
        var allButtons = ButtonState.getAllButtons();
        var buttons = new IThemeButton[allButtons.size()];
        for (int i = 0; i < allButtons.size(); i++) {
            var state = allButtons.get(i);
            buttons[i] = builder.getThemeToggleButton("gtceu.button." + state.name, state.name,
                    b -> {
                        ButtonState.toggleButton(state);
                        Arrays.stream(buttons).filter(btn -> btn.getToggled() || btn == b)
                                .forEach(IThemeButton::toggle);
                    });
            buttons[i].setToggled(state.enabled);
        }
        var toolbar = builder.getNewToolbar(buttons);

        var theme = ThemeLoader.getCurrentTheme();
        var screen = event.getFullscreen().getScreen();
        var toolbarTheme = theme.container.toolbar;
        var toolbarSpec = toolbarTheme.horizontal;
        var margin = toolbarSpec.margin;
        var padding = toolbarSpec.padding;
        var height = toolbarSpec.inner.height;
        toolbar.setLayoutHorizontal(margin, screen.height - (height + margin), padding, true);
    }
}
