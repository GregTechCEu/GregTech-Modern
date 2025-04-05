package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.xaeros.worldmap.gui.GuiTexturedButtonWithSize;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;
import xaero.map.gui.*;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin extends ScreenBase implements IRightClickableElement {

    @Shadow
    public abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addButton(T guiEventListener);

    protected GuiMapMixin(Screen parent, Screen escape, MapProcessor mapProcessor, Entity player) {
        super(parent, escape, Component.translatable("gui.xaero_world_map_screen"));
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void gtceu$injectInitGui(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) return;
        int startX, startY, xOffset, yOffset;

        switch (ConfigHolder.INSTANCE.compat.minimap.direction) {
            case VERTICAL -> {
                xOffset = 0;
                yOffset = 1;
            }
            case HORIZONTAL -> {
                xOffset = 1;
                yOffset = 0;
            }
            default -> throw new IllegalStateException(
                    "Unexpected value: " + ConfigHolder.INSTANCE.compat.minimap.direction);
        }

        switch (ConfigHolder.INSTANCE.compat.minimap.buttonAnchor) {
            case TOP_LEFT -> {
                startX = ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = ConfigHolder.INSTANCE.compat.minimap.yOffset;
            }
            case TOP_CENTER -> {
                startX = width / 2 + ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = ConfigHolder.INSTANCE.compat.minimap.yOffset;
            }
            case TOP_RIGHT -> {
                startX = width - 20 - ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = ConfigHolder.INSTANCE.compat.minimap.yOffset;
                xOffset = -xOffset;
            }
            case RIGHT_CENTER -> {
                startX = width - 20 - ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = height / 2 + ConfigHolder.INSTANCE.compat.minimap.yOffset;
                xOffset = -xOffset;
                yOffset = -yOffset;
            }
            case BOTTOM_RIGHT -> {
                startX = width - 20 - ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = height - 20 - ConfigHolder.INSTANCE.compat.minimap.yOffset;
                xOffset = -xOffset;
                yOffset = -yOffset;
            }
            case BOTTOM_CENTER -> {
                startX = width / 2 + ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = height - 20 - ConfigHolder.INSTANCE.compat.minimap.yOffset;
                yOffset = -yOffset;
            }
            case BOTTOM_LEFT -> {
                startX = ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = height - 20 - ConfigHolder.INSTANCE.compat.minimap.yOffset;
                yOffset = -yOffset;
            }
            case LEFT_CENTER -> {
                startX = ConfigHolder.INSTANCE.compat.minimap.xOffset;
                startY = height / 2 + ConfigHolder.INSTANCE.compat.minimap.yOffset;
                yOffset = -yOffset;
            }
            default -> throw new IllegalStateException(
                    "Unexpected value: " + ConfigHolder.INSTANCE.compat.minimap.buttonAnchor);
        }

        if (ConfigHolder.INSTANCE.compat.minimap.buttonAnchor.isCentered()) {
            int totalButtonSize = ButtonState.buttonAmount() * 10;
            if (ConfigHolder.INSTANCE.compat.minimap.buttonAnchor.usualDirection()
                    .equals(ConfigHolder.INSTANCE.compat.minimap.direction)) {
                startX -= xOffset * totalButtonSize;
                startY -= yOffset * totalButtonSize;
                if (xOffset < 0) startX -= 20;
                if (yOffset < 0) startY -= 20;
            } else {
                startX -= Math.abs(yOffset) * 10;
                startY -= Math.abs(xOffset) * 10;
            }
        }

        int offset = 0;
        for (ButtonState.Button button : ButtonState.getAllButtons()) {
            Button mapButton = new GuiTexturedButtonWithSize(
                    startX + (20 * xOffset * offset), startY + (20 * yOffset * offset), 20, 20,
                    0, ButtonState.isEnabled(button) ? 16 : 0, 16, 16, 16, 32,
                    GTCEu.id("textures/gui/widget/button_" + button.name + ".png"),
                    guiButton -> {
                        ButtonState.toggleButton(button);
                        init(minecraft, width, height);
                    },
                    () -> new CursorBox("gtceu.button." + button.name));

            addButton(mapButton);
            offset++;
        }
    }
}
