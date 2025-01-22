package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
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

    @Inject(method = "addWidgets", at = @At(value = "TAIL"))
    private void gtceu$injectAddWidgets(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration) return;
        gtceu$injectedWidgets.clear();
        var prefix = "gtceu.button.";
        for (var button : ButtonState.getAllButtons()) {
            var icon = switch (button.name) {
                case "ore_veins" -> ItemIcon.getItemIcon(Items.RAW_IRON);
                case "bedrock_fluids" -> ItemIcon.getItemIcon(Items.BUCKET);
                default -> Icons.INFO;
            };
            var buttonWidget = new SimpleButton(this, Component.translatable(prefix + button.name),
                    icon, (b, m) -> {
                        ButtonState.toggleButton(button);
                        refreshWidgets();
                    });
            add(buttonWidget);
            gtceu$injectedWidgets.add(buttonWidget);
        }
        var hideDepletedButton = new SimpleButton(this, Component.translatable("gtceu.button.hide_depleted"),
                ItemIcon.getItemIcon(Items.SPYGLASS), (b, m) -> {
                    FTBChunksOptions.toggleLayer("hide_depleted", !FTBChunksOptions.showLayer("hide_depleted"));
                }) {

            @Override
            public void addMouseOverText(TooltipList list) {
                var lang = prefix + (FTBChunksOptions.hideDepleted() ? "show_depleted" : "hide_depleted");
                list.add(Component.translatable(lang));
            }
        };
        add(hideDepletedButton);
        gtceu$injectedWidgets.add(hideDepletedButton);
    }

    @Inject(method = "alignWidgets", at = @At(value = "TAIL"))
    private void gtceu$injectAlignWidgets(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration) return;
        var buttonCount = gtceu$injectedWidgets.size();
        var startHeight = (height - buttonCount * 18) / 2;
        for (int i = 0; i < buttonCount; i++) {
            var buttonWidget = gtceu$injectedWidgets.get(i);
            buttonWidget.setPosAndSize(1, startHeight + i * 18, 16, 16);
        }
    }
}
