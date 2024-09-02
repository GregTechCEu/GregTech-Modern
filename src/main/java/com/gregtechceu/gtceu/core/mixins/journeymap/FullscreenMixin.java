package com.gregtechceu.gtceu.core.mixins.journeymap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapRenderer;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import journeymap.client.api.model.IFullscreen;
import journeymap.client.io.ThemeLoader;
import journeymap.client.model.MapState;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.RadarDrawStepFactory;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Fullscreen.class)
public abstract class FullscreenMixin extends JmUI implements IFullscreen {

    @Unique
    private JourneymapRenderer gtceu$renderer;

    @Shadow(remap = false)
    private ThemeToolbar mapTypeToolbar;

    @Shadow(remap = false) @Final
    private static MapState state;

    @Shadow(remap = false) @Final
    private static GridRenderer gridRenderer;

    @Shadow(remap = false)
    private FullMapProperties fullMapProperties;

    @Shadow @Final private RadarDrawStepFactory radarRenderer;
    @Shadow private Minecraft minecraft;
    @Unique private Map<String, ThemeToggle> gtceu$buttons;

    @Unique private ThemeToolbar gtceu$overlayToolbar;

    public FullscreenMixin(String title) {
        super(title);
    }

    @Inject(method = "initButtons",
            at = @At(value = "FIELD",
                     target = "Ljourneymap/client/ui/fullscreen/Fullscreen;mapTypeToolbar:Ljourneymap/client/ui/theme/ThemeToolbar;",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER
            ), remap = false
    )
    private void visualores$injectInitButtons(CallbackInfo ci) {
        final Theme theme = ThemeLoader.getCurrentTheme();
        gtceu$buttons = new LinkedHashMap<>();

        for (ButtonState.Button button : ButtonState.getAllButtons()) {
            ThemeToggle mapButton = new ThemeToggle(theme, "gtceu.button." + button.name, button.name, b -> {});
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
        }
        else {
            // jank to not have to add an accessor/at
            this.mapTypeToolbar.reverse();
            this.mapTypeToolbar.reverse().addAll(0, allButtons);
        }


        gtceu$renderer = new JourneymapRenderer((Fullscreen) (Object) this);
    }

    @Inject(method = "layoutButtons", at = @At("TAIL"), remap = false)
    private void visualores$injectLayoutButtons(CallbackInfo ci) {
        for (String buttonName : gtceu$buttons.keySet()) {
            gtceu$buttons.get(buttonName).setToggled(ButtonState.isEnabled(buttonName), false);
        }

        if (ConfigHolder.INSTANCE.compat.minimap.rightToolbar) {
            gtceu$overlayToolbar.layoutCenteredVertical(width - gtceu$overlayToolbar.getHMargin(), height / 2, false, mapTypeToolbar.getToolbarSpec().padding);
        }
    }

    @WrapOperation(method = "drawMap",
                   at = @At(value = "INVOKE", target = "Ljourneymap/client/render/map/GridRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;DDDD)V", ordinal = 1),
                   remap = false
    )
    private void visualores$injectDrawMap(GridRenderer instance, GuiGraphics graphics, List<? extends DrawStep> drawStepList, double xOffset, double yOffset, double fontScale, double rotation, Operation<Void> original) {
        original.call(instance, graphics, drawStepList, xOffset, yOffset, fontScale, rotation);

        float scale = (float) Math.pow(2, fullMapProperties.zoomLevel.get());
        double rw = minecraft.getWindow().getScreenWidth() / scale;
        double rh = minecraft.getWindow().getScreenHeight() / scale;
        gtceu$renderer.updateVisibleArea(state.getDimension(), (int) (gridRenderer.getCenterBlockX() - xOffset / scale - rw / 2), (int) (gridRenderer.getCenterBlockZ() - yOffset / scale - rh / 2), (int) rw, (int) rh);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, 0);
        poseStack.scale(scale, scale, 1);
        poseStack.translate(rw / 2 - gridRenderer.getCenterBlockX(), rh / 2 - gridRenderer.getCenterBlockZ(), 0);

        gtceu$renderer.render(graphics, gridRenderer.getCenterBlockX(), gridRenderer.getCenterBlockZ(), scale);

        poseStack.popPose();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = At.Shift.BY, by = -2, ordinal = 0))
    private void visualores$injectTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                          CallbackInfo ci, @Local List<String> tooltip) {
        double scale = Math.pow(2, fullMapProperties.zoomLevel.get());
        gtceu$renderer.updateHovered(mouseX, mouseY, gridRenderer.getCenterBlockX(), gridRenderer.getCenterBlockZ(), scale);
        if (tooltip == null || tooltip.isEmpty()) {
            gtceu$renderer.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void visualores$injectKeyPress(int key, int value, int modifier, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBind.ACTION.toMinecraft().getKey().getValue() == key) {
            gtceu$renderer.onActionKey();
        }
    }

    @Inject(method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Ljourneymap/client/ui/fullscreen/layer/LayerDelegate;onMouseClicked(Lnet/minecraft/client/Minecraft;Ljourneymap/client/render/map/GridRenderer;Ljava/awt/geom/Point2D$Double;IF)V", remap = false),
            cancellable = true)
    private void visualores$injectMouseClicked(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (mouseButton == 0) {
            if (gtceu$renderer.onClick(mouseX, mouseY)) {
                cir.cancel();
            }
        }
    }
}
