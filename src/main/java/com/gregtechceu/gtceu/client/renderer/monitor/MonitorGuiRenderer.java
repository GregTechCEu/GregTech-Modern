package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.MonitorPartMachine;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.core.mixins.client.GuiGraphicsAccessor;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import org.joml.Vector2d;

public class MonitorGuiRenderer implements IMonitorRenderer {

    private final ModularScreen screen;
    private final Screen vanillaScreen;
    private int width = 200, height = 200;

    public MonitorGuiRenderer(Pair<Level, BlockPos> target) {
        if (target.getSecond() == null) {
            screen = null;
            vanillaScreen = null;
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
            ModularPanel mainPanel = factory.createPanel(data, syncManager, settings);
            WidgetTree.collectSyncValues(syncManager, mainPanel);
            screen = factory.createScreen(data, mainPanel);
            int windowId = GTValues.RNG.nextInt();
            ModularContainerMenu menu = settings.hasContainer() ? settings.createContainer(windowId) :
                    factory.createContainer(windowId);
            menu.construct(MCHelper.getPlayer(), syncManager, settings, mainPanel.getName(), data);
            vanillaScreen = factory.createScreenWrapper(menu, screen).getWrappedScreen();
            vanillaScreen.init(MCHelper.getMc(), width, height);
            screen.onResize(width, height);
        } else {
            screen = null;
            vanillaScreen = null;
        }
    }

    public void renderGui(int maxWidth, int maxHeight, PoseStack poseStack, MultiBufferSource buffer,
                          float partialTick, int mouseX, int mouseY) {
        GuiGraphics guiGraphics = new GuiGraphics(MCHelper.getMc(), (MultiBufferSource.BufferSource) buffer);
        poseStack.scale(1 / 256f, 1 / 256f, 1 / 256e3f);
        ((GuiGraphicsAccessor) guiGraphics).setPose(poseStack);
        screen.getContext().setGraphics(guiGraphics);
        boolean resized = false;
        if (maxWidth * 256 != width) {
            width = maxWidth * 256;
            resized = true;
        }
        if (maxHeight * 256 != height) {
            height = maxHeight * 256;
            resized = true;
        }
        if (resized) screen.onResize(width, height);
        ClientScreenHandler.drawScreen(guiGraphics, screen, vanillaScreen, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (screen == null) return;
        BlockPos rel = group.getRow(0, machine::toRelative).get(0);
        BlockPos size = GTUtil.getLast(group.getRow(-1, machine::toRelative))
                .offset(-rel.getX() + 1, -rel.getY() + 1, -rel.getZ() + 1);
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        Player player = MCHelper.getPlayer();
        HitResult hit = player.pick(player.getAttributeValue(ForgeMod.BLOCK_REACH.get()), partialTick, false);
        double mouseX = 0, mouseY = 0;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockPos relPos = machine.toRelative(pos);
            if (MetaMachine.getMachine(player.level(), pos) instanceof MonitorPartMachine monitor) {
                Vector2d monitorMousePos = monitor.getMousePos(hit);
                mouseX = relPos.getX() - rel.getX() + monitorMousePos.x();
                mouseY = relPos.getY() - rel.getY() + monitorMousePos.y();
            }
        }
        renderGui(size.getX(), size.getY(), poseStack, buffer, partialTick, (int) (mouseX * 256), (int) (mouseY * 256));
    }
}
