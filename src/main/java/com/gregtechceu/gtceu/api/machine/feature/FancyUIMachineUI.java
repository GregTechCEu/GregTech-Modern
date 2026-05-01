package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.fancy.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

final class FancyUIMachineUI {

    private FancyUIMachineUI() {}

    static ModularUI createUI(IFancyUIMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer).widget(new FancyMachineUIWidget(machine, 176, 166));
    }

    static Widget createMainPage(IFancyUIMachine machine, FancyMachineUIWidget widget) {
        EditableMachineUI editableUI = machine.self().getDefinition().getEditableUI();
        if (editableUI != null) {
            var template = editableUI.createCustomUI();
            if (template == null) {
                template = editableUI.createDefault();
            }
            editableUI.setupUI(template, machine.self());
            return template;
        }
        return machine.createUIWidget();
    }

    static WidgetGroup createDefaultWidget(IFancyUIMachine machine) {
        var group = new WidgetGroup(0, 0, 100, 100);
        if (machine.isRemote()) {
            group.addWidget(new ImageWidget((100 - 48) / 2, 60, 48, 16, GuiTextures.SCENE));
            TrackedDummyWorld world = new TrackedDummyWorld();
            world.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(machine.self().getBlockState()));
            SceneWidget sceneWidget = new SceneWidget(0, 0, 100, 100, world) {

                @Override
                @OnlyIn(Dist.CLIENT)
                public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY,
                                             float partialTicks) {
                    if (renderer != null) {
                        this.rotationPitch = (partialTicks + getGui().getTickCount()) * 2;
                        renderer.setCameraLookAt(this.center, 0.1f, Math.toRadians(this.rotationPitch),
                                Math.toRadians(this.rotationYaw));
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

    static IGuiTexture getTabIcon(IFancyUIMachine machine) {
        return new ItemStackTexture(machine.self().getDefinition().getItem());
    }

    static void attachSideTabs(IFancyUIMachine machine, TabsWidget sideTabs) {
        sideTabs.setMainTab(machine);

        if (machine instanceof IRecipeLogicMachine rLMachine && rLMachine.getRecipeTypes().length > 1) {
            sideTabs.attachSubTab(new MachineModeFancyConfigurator(rLMachine));
        }
        var directionalConfigurator = CombinedDirectionalFancyConfigurator.of(machine.self(), machine.self());
        if (directionalConfigurator != null)
            sideTabs.attachSubTab(directionalConfigurator);
    }

    static void attachConfigurators(IFancyUIMachine provider, ConfiguratorPanel configuratorPanel) {
        if (provider instanceof IControllable controllable) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                    controllable::isWorkingEnabled, (clickData, pressed) -> controllable.setWorkingEnabled(pressed))
                    .setTooltipsSupplier(pressed -> List.of(
                            Component.translatable(
                                    pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"))));
        }
        if (provider instanceof MetaMachine machine) {
            for (var direction : Direction.values()) {
                if (machine.getCoverContainer().hasCover(direction)) {
                    var configurator = machine.getCoverContainer().getCoverAtSide(direction).getConfigurator();
                    if (configurator != null)
                        configuratorPanel.attachConfigurators(configurator);
                }
            }
        }
    }

    static void attachTooltips(IFancyUIMachine provider, TooltipsPanel tooltipsPanel) {
        tooltipsPanel.attachTooltips(provider.self());
        provider.self().getTraitHolder().getAllTraits().stream().filter(IFancyTooltip.class::isInstance)
                .map(IFancyTooltip.class::cast)
                .forEach(tooltipsPanel::attachTooltips);
    }
}
