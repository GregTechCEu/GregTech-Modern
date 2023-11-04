package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.*;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.AutoOutputFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.OverclockFancyConfigurator;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/28
 * @implNote IFancyUIMachine
 */
public interface IFancyUIMachine extends IUIMachine, IFancyUIProvider {
    @Override
    default ModularUI createUI(Player entityPlayer) {
        return new ModularUI(200, 214, this, entityPlayer).widget(new FancyMachineUIWidget(this));
    }

    /**
     * We should not override this method in general, and use {@link IFancyUIMachine#createUIWidget()} instead,
     */
    @Override
    default Widget createMainPage() {
        var editableUI = self().getDefinition().getEditableUI();
        if (editableUI != null) {
            var template = editableUI.createCustomUI();
            if (template == null) {
                template = editableUI.createDefault();
            }
            editableUI.setupUI(template, self());
            return template;
        }
        return createUIWidget();
    }

    /**
     * Create the core widget of this machine.
     */
    default Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 100, 100);
        if (isRemote()) {
            group.addWidget(new ImageWidget((100 - 48) / 2, 60, 48, 16, GuiTextures.SCENE));
            TrackedDummyWorld world = new TrackedDummyWorld();
            world.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(self().getBlockState()));
            SceneWidget sceneWidget = new SceneWidget(0, 0, 100, 100, world) {
                @Override
                @Environment(EnvType.CLIENT)
                public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    // AUTO ROTATION
                    if (renderer != null) {
                        this.rotationPitch = (partialTicks + getGui().getTickCount()) * 2;
                        renderer.setCameraLookAt(this.center, 0.1f, Math.toRadians(this.rotationPitch), Math.toRadians(this.rotationYaw));
                    }
                    super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                }
            };
            sceneWidget.useOrtho(true)
                    .setOrthoRange(0.5f)
                    .setScalable(false)
                    .setDraggable(false)
                    .setRenderFacing(false)
                    .setRenderSelect(false);
            sceneWidget.getRenderer().setFov(30);
            group.addWidget(sceneWidget);
            sceneWidget.setRenderedCore(List.of(BlockPos.ZERO), null);
        }
        return group;
    }

    @Override
    default IGuiTexture getTabIcon() {
        return new ItemStackTexture(self().getDefinition().getItem());
    }

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        if (this instanceof IControllable controllable) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                    controllable::isWorkingEnabled, (clickData, pressed) -> controllable.setWorkingEnabled(pressed))
                    .setTooltipsSupplier(pressed -> List.of(
                            Component.translatable(pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled")
                    )));
        }
        if (this instanceof IRecipeLogicMachine rLMachine && rLMachine.getRecipeTypes().length > 1) {
            configuratorPanel.attachConfigurators(new MachineModeFancyConfigurator(rLMachine));
        }
        configuratorPanel.attachConfigurators(self().getCoverContainer());
        if (this instanceof IAutoOutputItem || this instanceof IAutoOutputFluid) {
            configuratorPanel.attachConfigurators(new AutoOutputFancyConfigurator(self()));
        }
        if (this instanceof IOverclockMachine overclockMachine) {
            configuratorPanel.attachConfigurators(new OverclockFancyConfigurator(overclockMachine));
        }
    }

    @Override
    default void attachTooltips(TooltipsPanel tooltipsPanel) {
        tooltipsPanel.attachTooltips(self());
        self().getTraits().stream().filter(IFancyTooltip.class::isInstance).map(IFancyTooltip.class::cast).forEach(tooltipsPanel::attachTooltips);
    }

    @Override
    default List<Component> getTabTooltips() {
        var list = new ArrayList<Component>();
        list.add(Component.translatable(self().getDefinition().getDescriptionId()));
        return list;
    }
}
