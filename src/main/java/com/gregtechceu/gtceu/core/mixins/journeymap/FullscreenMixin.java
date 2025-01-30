package com.gregtechceu.gtceu.core.mixins.journeymap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;

import journeymap.client.api.model.IFullscreen;
import journeymap.client.io.ThemeLoader;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = Fullscreen.class, remap = false)
public abstract class FullscreenMixin extends JmUI implements IFullscreen {

    @Shadow(remap = false)
    private ThemeToolbar mapTypeToolbar;
    @Unique
    private Map<String, ThemeToggle> gtceu$buttons;

    @Unique
    private ThemeToolbar gtceu$overlayToolbar;

    public FullscreenMixin(String title) {
        super(title);
    }

    @Inject(method = "initButtons",
            at = @At(value = "FIELD",
                     target = "Ljourneymap/client/ui/fullscreen/Fullscreen;mapTypeToolbar:Ljourneymap/client/ui/theme/ThemeToolbar;",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER),
            remap = false)
    private void gtceu$injectInitButtons(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) return;
        final Theme theme = ThemeLoader.getCurrentTheme();
        gtceu$buttons = new LinkedHashMap<>();

        for (ButtonState.Button button : ButtonState.getAllButtons()) {
            ThemeToggle mapButton = this
                    .addRenderableWidget(new ThemeToggle(theme, "gtceu.button." + button.name, button.name, b -> {
                        ButtonState.toggleButton(button);
                    }));
            mapButton.setToggled(ButtonState.isEnabled(button), false);
            mapButton.setEnabled(true);
            mapButton.addToggleListener((onOffButton, b) -> {
                ButtonState.toggleButton(button);

                return true;
            });

            gtceu$buttons.put(button.name, mapButton);
        }

        List<ThemeToggle> allButtons = new ArrayList<>(gtceu$buttons.values());
        Collections.reverse(allButtons);

        if (ConfigHolder.INSTANCE.compat.minimap.rightToolbar) {
            gtceu$overlayToolbar = new ThemeToolbar(theme, allButtons.toArray(Button[]::new));
            gtceu$overlayToolbar.setLayout(ButtonList.Layout.Vertical, ButtonList.Direction.RightToLeft);
            gtceu$overlayToolbar.addAllButtons((Fullscreen) (Object) this);
        } else {
            // jank to not have to add an accessor/at
            this.mapTypeToolbar.reverse();
            this.mapTypeToolbar.reverse().addAll(0, allButtons);
        }
    }

    @Inject(method = "layoutButtons", at = @At("TAIL"), remap = false)
    private void gtceu$injectLayoutButtons(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) return;
        for (String buttonName : gtceu$buttons.keySet()) {
            gtceu$buttons.get(buttonName).setToggled(ButtonState.isEnabled(buttonName), false);
        }

        if (ConfigHolder.INSTANCE.compat.minimap.rightToolbar) {
            gtceu$overlayToolbar.layoutCenteredVertical(width - gtceu$overlayToolbar.getHMargin(), height / 2, false,
                    mapTypeToolbar.getToolbarSpec().padding);
        }
    }
}
