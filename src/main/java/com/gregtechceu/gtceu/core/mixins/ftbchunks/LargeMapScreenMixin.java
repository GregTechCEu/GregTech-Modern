package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ButtonState;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = LargeMapScreen.class, remap = false)
public abstract class LargeMapScreenMixin extends BaseScreen {

    @Unique
    private final List<Button> gtceu$injectedWidgets = new ArrayList<>();

    @Inject(method = "addWidgets",
            at = @At(value = "TAIL"))
    private void gtceu$injectAddWidgets(CallbackInfo ci) {
        gtceu$injectedWidgets.clear();
        for (ButtonState.Button button : ButtonState.getAllButtons()) {
            Icon icon = switch (button.name) {
                case "ore_veins" -> ItemIcon.getItemIcon(Items.RAW_IRON);
                case "bedrock_fluids" -> ItemIcon.getItemIcon(Items.BUCKET);
                default -> Icons.INFO;
            };
            SimpleButton buttonWidget = new SimpleButton(this, Component.translatable("gtceu.button." + button.name),
                    icon, (b, m) -> {
                        ButtonState.toggleButton(button);
                        this.refreshWidgets();
                    });
            this.add(buttonWidget);
            gtceu$injectedWidgets.add(buttonWidget);
        }
    }

    @Inject(method = "alignWidgets", at = @At(value = "TAIL"))
    private void gtceu$injectAlignWidgets(CallbackInfo ci) {
        int buttonCount = gtceu$injectedWidgets.size();
        int startHeight = (this.height - buttonCount * 18) / 2;
        for (int i = 0; i < buttonCount; i++) {
            Button buttonWidget = gtceu$injectedWidgets.get(i);
            buttonWidget.setPosAndSize(1, startHeight + i * 18, 16, 16);
        }
    }
}
