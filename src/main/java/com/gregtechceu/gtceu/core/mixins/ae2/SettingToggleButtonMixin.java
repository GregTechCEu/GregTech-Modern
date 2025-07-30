package com.gregtechceu.gtceu.core.mixins.ae2;

import net.minecraft.network.chat.Component;

import appeng.api.config.PowerUnits;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton.IHandler;
import appeng.core.localization.ButtonToolTips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Reasoning: The Appearance map is used to get
// appearances for buttons. This Enum adds
// the EU power unit so that it appears
// correctly in controller and network tool UI.
@Mixin(value = SettingToggleButton.class, remap = false)
public abstract class SettingToggleButtonMixin<T extends Enum<T>> {

    @Invoker("registerApp")
    private static <E extends Enum<E>> void invokeRegisterApp(Icon icon, Setting<E> setting, E val,
                                                              ButtonToolTips title, Component... tooltipLines) {
        throw new AssertionError();
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void gtceu$registerEUPowerUnit(Setting<T> setting, T val,
                                           IHandler<SettingToggleButton<T>> onPress, CallbackInfo ci) {
        if (setting == Settings.POWER_UNITS) {
            PowerUnits eu = PowerUnits.valueOf("EU");
            invokeRegisterApp(Icon.POWER_UNIT_EU, Settings.POWER_UNITS, eu, ButtonToolTips.PowerUnits,
                    eu.textComponent());
        }
    }
}
