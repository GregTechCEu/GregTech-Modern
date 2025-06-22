package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IFancyUIMachine extends IUIMachine, IFancyUIProvider {

    @Override
    default ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer).widget(new FancyMachineUIWidget(this, 176, 166));
    }

    /**
     * We should not override this method in general, and use {@link IFancyUIMachine#createUIWidget()} instead,
     */
    @Override
    default Widget createMainPage(FancyMachineUIWidget widget) {
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
                @OnlyIn(Dist.CLIENT)
                public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY,
                                             float partialTicks) {
                    // AUTO ROTATION
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

    @Override
    default IGuiTexture getTabIcon() {
        return new ItemStackTexture(self().getDefinition().getItem());
    }

    @Override
    default void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);

        if (this instanceof IRecipeLogicMachine rLMachine && rLMachine.getRecipeTypes().length > 1) {
            sideTabs.attachSubTab(new MachineModeFancyConfigurator(rLMachine));
        }
        var directionalConfigurator = CombinedDirectionalFancyConfigurator.of(self(), self());
        if (directionalConfigurator != null)
            sideTabs.attachSubTab(directionalConfigurator);
    }

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        if (this instanceof IControllable controllable) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                    controllable::isWorkingEnabled, (clickData, pressed) -> controllable.setWorkingEnabled(pressed))
                    .setTooltipsSupplier(pressed -> List.of(
                            Component.translatable(
                                    pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"))));
        }
        if (this instanceof MetaMachine machine) {
            for (var direction : Direction.values()) {
                if (machine.getCoverContainer().hasCover(direction)) {
                    var configurator = machine.getCoverContainer().getCoverAtSide(direction).getConfigurator();
                    if (configurator != null)
                        configuratorPanel.attachConfigurators(configurator);
                }
            }
        }
    }

    @Override
    default void attachTooltips(TooltipsPanel tooltipsPanel) {
        tooltipsPanel.attachTooltips(self());
        self().getTraits().stream().filter(IFancyTooltip.class::isInstance).map(IFancyTooltip.class::cast)
                .forEach(tooltipsPanel::attachTooltips);
    }

    @Override
    default List<Component> getTabTooltips() {
        var list = new ArrayList<Component>();
        list.add(Component.translatable(self().getDefinition().getDescriptionId()));
        return list;
    }

    @Override
    default Component getTitle() {
        return Component.translatable(self().getDefinition().getDescriptionId());
    }
}
