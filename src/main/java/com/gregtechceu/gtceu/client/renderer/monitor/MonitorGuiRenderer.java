package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.InWorldMUIOpenEvent;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.client.mui.screen.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.AdvancedMonitorPartMachine;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class MonitorGuiRenderer implements IMonitorRenderer {

    private ModularScreen screen;
    private Screen vanillaScreen;
    private ModularContainerMenu menu;
    private int width = 200, height = 200;
    private final Level targetLevel;
    private final BlockPos targetPos;

    public MonitorGuiRenderer(Pair<Level, BlockPos> target) {
        MinecraftForge.EVENT_BUS.register(this);
        this.targetLevel = target.getFirst();
        this.targetPos = target.getSecond();
        if (target.getSecond() == null) {
            screen = null;
            vanillaScreen = null;
            return;
        }
        MetaMachine targetMachine = MetaMachine.getMachine(target.getFirst(), target.getSecond());
        if (targetMachine != null &&
                (targetMachine.getDefinition().getUI() != null || targetMachine instanceof IMuiMachine)) {
            GuiManager.openFromClient(MachineUIFactory.INSTANCE,
                    new PosGuiData(MCHelper.getPlayer(), target.getSecond()), true);
        } else {
            screen = null;
            vanillaScreen = null;
        }
    }

    @SubscribeEvent
    public void openClientInWorldUI(InWorldMUIOpenEvent event) {
        if (this.targetPos != null && event.getGuiData() instanceof PosGuiData posGuiData) {
            if (posGuiData.getBlockPos().asLong() == targetPos.asLong() && posGuiData.getLevel() == targetLevel) {
                this.menu = event.getMenu();
                this.screen = event.getScreen();
                this.vanillaScreen = event.getVanillaScreen();
                this.vanillaScreen.init(MCHelper.getMc(), this.width, this.height);
                this.screen.onResize(this.width, this.height);
                ModularPanel mainPanel = this.screen.getMainPanel();
                for (IWidget child : mainPanel.getChildren()) {
                    if (child instanceof SlotGroupWidget slotGroupWidget && slotGroupWidget.isPlayerInventory()) {
                        slotGroupWidget.disabled();
                        mainPanel.height(mainPanel.getArea().height - slotGroupWidget.getArea().height);
                        mainPanel.scheduleResize();
                    }
                }
            }
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
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        screen.onFrameUpdate();
        ClientScreenHandler.drawScreen(guiGraphics, screen, vanillaScreen, mouseX, mouseY, partialTick);
        ClientScreenHandler.drawDebugScreen(guiGraphics, screen, screen);
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (screen == null || group.isEmpty()) return;
        BlockPos rel = group.getRow(0, machine::toRelative).get(0);
        BlockPos size = GTUtil.getLast(group.getRow(-1, machine::toRelative))
                .offset(-rel.getX() + 1, -rel.getY() + 1, -rel.getZ() + 1);
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        Player player = MCHelper.getPlayer();
        HitResult hit = player.pick(player.getAttributeValue(ForgeMod.BLOCK_REACH.get()), partialTick, false);
        double mouseX = -1, mouseY = -1;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockPos relPos = machine.toRelative(pos);
            if (MetaMachine.getMachine(player.level(), pos) instanceof MonitorPartMachine monitor &&
                    monitor.getControllers().contains(machine)) {
                Vector2d monitorMousePos = monitor.getMousePos(hit);
                mouseX = relPos.getX() - rel.getX() + monitorMousePos.x();
                mouseY = relPos.getY() - rel.getY() + 1 - monitorMousePos.y();
                if (monitor instanceof AdvancedMonitorPartMachine advancedMonitor && mouseX >= 0 && mouseY >= 0 && mouseX <= width && mouseY <= height) {
                    if (advancedMonitor.isClickedThisFrame()) {
                        this.screen.onMousePressed(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        this.vanillaScreen.mouseClicked(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        advancedMonitor.setClickedThisFrame(false);
                    }
                }
            }
        }
        renderGui(size.getX(), size.getY(), poseStack, buffer, partialTick, (int) (mouseX * 256), (int) (mouseY * 256));
    }
}
