package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.IXaeroMinimap;
import xaero.common.misc.Misc;
import xaero.map.MapProcessor;
import xaero.map.controls.KeyConflictContext;
import xaero.map.gui.CursorBox;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiTexturedButton;
import xaero.minimap.XaeroMinimap;

@Mixin(GuiMap.class)
public abstract class GuiMapMixin extends Screen {
    @Shadow(remap = false) private MapProcessor mapProcessor;

    @Shadow(remap = false) private double scale;
    @Shadow(remap = false) private double cameraX;
    @Shadow(remap = false) private double cameraZ;

    @Shadow public abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addButton(T guiEventListener);

    @Unique
    private GenericMapRenderer gtceu$renderer;

    protected GuiMapMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gtceu$injectInitGui(CallbackInfo ci) {
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
            default -> throw new IllegalStateException("Unexpected value: " + ConfigHolder.INSTANCE.compat.minimap.direction);
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
            default -> throw new IllegalStateException("Unexpected value: " + ConfigHolder.INSTANCE.compat.minimap.buttonAnchor);
        }

        if (ConfigHolder.INSTANCE.compat.minimap.buttonAnchor.isCentered()) {
            int totalButtonSize = ButtonState.buttonAmount() * 10;
            if (ConfigHolder.INSTANCE.compat.minimap.buttonAnchor.usualDirection().equals(ConfigHolder.INSTANCE.compat.minimap.direction)) {
                startX -= xOffset * totalButtonSize;
                startY -= yOffset * totalButtonSize;
                if (xOffset < 0) startX -= 20;
                if (yOffset < 0) startY -= 20;
            }
            else {
                startX -= Math.abs(yOffset) * 10;
                startY -= Math.abs(xOffset) * 10;
            }
        }

        int offset = 0;
        for (ButtonState.Button button : ButtonState.getAllButtons()) {
            Button mapButton = new GuiTexturedButton(
                    startX + (20 * xOffset * offset), startY + (20 * yOffset * offset), 20, 20,
                    ButtonState.isEnabled(button) ? 16 : 0, 0, 16, 16,
                    GTCEu.id("textures/gui/widget/button_" + button.name + ".png"),
                    guiButton -> {
                        ButtonState.toggleButton(button);
                        //setWorldAndResolution(mc, width, height);
                    },
                    () -> new CursorBox("gtceu.button." + button.name)
            );

            addButton(mapButton);
            offset++;
        }

        gtceu$renderer = new GenericMapRenderer((GuiMap) (Object) this);
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lxaero/map/element/MapElementRenderHandler;render(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;", remap = false),
                    to = @At(value = "INVOKE", target = "Lxaero/map/MapProcessor;getFootprints()Ljava/util/ArrayList;", remap = false)
            )
    )
    private void gtceu$injectDraw(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        double rw = Minecraft.getInstance().getWindow().getScreenWidth() / scale;
        double rh = Minecraft.getInstance().getWindow().getScreenHeight() / scale;
        gtceu$renderer.updateVisibleArea(mapProcessor.getMapWorld().getCurrentDimensionId(), (int) (cameraX - rw / 2), (int) (cameraZ - rh / 2), (int) (rw), (int) (rh));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-cameraX, -cameraZ, 0);
        gtceu$renderer.render(guiGraphics, cameraX, cameraZ, (float) scale);

        guiGraphics.pose().popPose();
    }

    // so buttons with semi-transparent regions render correctly
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lxaero/map/gui/ScreenBase;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void gtceu$injectDrawButtons(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        RenderSystem.enableBlend();
    }

    @Inject(method = "render",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lxaero/map/MapProcessor;renderThreadPauseSync:Ljava/lang/Object;", remap = false),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lxaero/map/gui/GuiMap;renderTooltips(Lnet/minecraft/client/gui/GuiGraphics;IIF)Z", remap = false))
    )
    private void gtceu$injectTooltip(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        gtceu$renderer.updateHovered(scaledMouseX, scaledMouseY, cameraX, cameraZ, scale);
        gtceu$renderer.renderTooltip(guiGraphics, scaledMouseX, scaledMouseY);
    }

    @Inject(method = "onInputPress", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtceu$injectKeyPress(InputConstants.Type type, int code, CallbackInfoReturnable<Boolean> cir) {
        if (Misc.inputMatchesKeyBinding(XaeroMinimap.instance, type, code, KeyBind.ACTION.toMinecraft(), KeyConflictContext.GUI) && gtceu$renderer.onActionKey()) {
            cir.setReturnValue(true);
        }
    }

    // no need to cancel if something was done, mapClicked normally only does stuff on right click
    @Inject(method = "mapClicked", at = @At("TAIL"), remap = false)
    private void gtceu$injectMapClicked(int button, int x, int y, CallbackInfo ci) {
        if (button == 0) {
            gtceu$renderer.onClick(x, y);
        }
    }
}
