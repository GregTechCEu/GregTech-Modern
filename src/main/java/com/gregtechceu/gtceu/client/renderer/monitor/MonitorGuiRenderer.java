package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.core.mixins.client.GuiGraphicsAccessor;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

public class MonitorGuiRenderer implements IMonitorRenderer {

    private final ModularScreen screen;
    private int width = 200, height = 200;

    public MonitorGuiRenderer(Pair<Level, BlockPos> target) {
        GTCEu.LOGGER.info("constructor called");
        if (target.getSecond() == null) {
            screen = null;
            return;
        }
        MetaMachine targetMachine = MetaMachine.getMachine(target.getFirst(), target.getSecond());
        if (targetMachine != null &&
                (targetMachine.getDefinition().getUI() != null || targetMachine instanceof IMuiMachine)) {
            UISettings settings = new UISettings();
            settings.getXeiSettings().forceDisabled();
            UIFactory<PosGuiData> factory = MachineUIFactory.INSTANCE;
            PosGuiData data = new PosGuiData(MCHelper.getPlayer(), target.getSecond());
            PanelSyncManager syncManager = new PanelSyncManager(true);
            ModularPanel panel = factory.createPanel(data, syncManager, settings);
            WidgetTree.collectSyncValues(syncManager, panel);
            screen = factory.createScreen(data, panel);
            int windowId = GTValues.RNG.nextInt();
            ModularContainerMenu menu = settings.hasContainer() ? settings.createContainer(windowId) :
                    factory.createContainer(windowId);
            menu.construct(MCHelper.getPlayer(), syncManager, settings, panel.getName(), data);
            screen.onResize(width, height);
        } else screen = null;
    }

    public void renderGui(int maxWidth, int maxHeight, PoseStack poseStack, MultiBufferSource buffer,
                          float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), (MultiBufferSource.BufferSource) buffer);
        ((GuiGraphicsAccessor) guiGraphics).setPose(poseStack);
        screen.getContext().setGraphics(guiGraphics);
        boolean resized = false;
        if (maxWidth * 16 != width) {
            width = maxWidth * 16;
            resized = true;
        }
        if (maxHeight * 16 != height) {
            height = maxHeight * 16;
            resized = true;
        }
        if (resized) screen.onResize(width, height);
        screen.render(guiGraphics, 123, 123, partialTick);
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (screen == null) return;
        BlockPos rel = group.getRow(0, machine::toRelative).get(0);
        BlockPos size = GTUtil.getLast(group.getRow(-1, machine::toRelative))
                .offset(-rel.getX() + 1, -rel.getY() + 1, -rel.getZ() + 1);
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        renderGui(size.getX(), size.getY(), poseStack, buffer, partialTick);
    }
}
