package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.InWorldMUIOpenEvent;
import com.gregtechceu.gtceu.api.mui.InWorldMUIRenderEvent;
import com.gregtechceu.gtceu.api.mui.animation.AnimatorManager;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.client.CharTypedEvent;
import com.gregtechceu.gtceu.client.EarlyKeyPressEvent;
import com.gregtechceu.gtceu.client.mui.screen.*;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.AdvancedMonitorPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.MonitorPartMachine;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class MonitorGuiRenderer implements IMonitorRenderer {

    private ModularScreen screen;
    private Screen vanillaScreen;
    private int width = 200, height = 200;
    private int mouseX = -1, mouseY = -1;
    private final Level targetLevel;
    private final BlockPos targetPos;
    private final RenderTarget renderTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);

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

    @SubscribeEvent
    public void keyPressedEvent(EarlyKeyPressEvent event) {
        if (mouseX < 0 || mouseX > width || mouseY < 0 || mouseY > height || MCHelper.getCurrentScreen() != null)
            return;
        screen.getContext().updateLatestKey(event.getKey(), event.getScanCode(), event.getModifiers());
        boolean early = ClientScreenHandler.handleKeyboardInput(screen, vanillaScreen,
                event.getAction() == InputConstants.PRESS,
                ClientScreenHandler.InputPhase.EARLY, event.getKey(), event.getScanCode(), event.getModifiers());
        if (early) event.setCanceled(true);
        else {
            boolean late = ClientScreenHandler.handleKeyboardInput(screen, vanillaScreen,
                    event.getAction() == InputConstants.PRESS,
                    ClientScreenHandler.InputPhase.LATE, event.getKey(), event.getScanCode(), event.getModifiers());
            if (late) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void charTyped(CharTypedEvent event) {
        if (mouseX < 0 || mouseX > width || mouseY < 0 || mouseY > height || MCHelper.getCurrentScreen() != null)
            return;
        screen.getContext().updateLatestTypedChar(event.getCodepoint(), event.getModifiers());
        if (screen.charTyped(event.getCodepoint(), event.getModifiers())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onInWorldGuiRender(InWorldMUIRenderEvent event) {
        renderGuiToBuffer(event.getGraphics(), event.getPartialTick());
    }

    public void renderGuiToBuffer(GuiGraphics guiGraphics, float partialTick) {
        if (screen == null || MCHelper.getPlayer() == null) return;
        screen.getContext().setGraphics(guiGraphics);
        screen.onFrameUpdate();
        AnimatorManager.INSTANCE.onDraw(null);
        if (width * height == 0) return;
        renderTarget.resize(width, height, Minecraft.ON_OSX);
        renderTarget.enableStencil();
        renderTarget.bindWrite(true);
        ClientScreenHandler.drawScreen(guiGraphics, screen, vanillaScreen, mouseX, mouseY, partialTick);
        ClientScreenHandler.drawDebugScreen(guiGraphics, screen, screen);
        renderTarget.unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    public void renderGui(int maxWidth, int maxHeight, PoseStack poseStack, MultiBufferSource buffer,
                          float partialTick, int mouseX, int mouseY) {
        poseStack.scale(1 / 256f, 1 / 256f, 1 / 256e3f);
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
        AnimatorManager.INSTANCE.onDraw(null);
        if (width * height == 0) return;
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.inWorldGui());
        RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());
        Matrix4f pose = poseStack.last().pose();
        consumer.vertex(pose, 0, height, 0).color(0xFFFFFFFF).uv(0, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, width, height, 0).color(0xFFFFFFFF).uv(1, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, width, 0, 0).color(0xFFFFFFFF).uv(1, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, 0, 0, 0).color(0xFFFFFFFF).uv(0, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
                if (monitor instanceof AdvancedMonitorPartMachine advancedMonitor && mouseX >= 0 && mouseY >= 0 &&
                        mouseX <= width && mouseY <= height) {
                    if (advancedMonitor.isClickedThisFrame()) {
                        this.screen.onMousePressed(mouseX * 256, mouseY * 256, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        this.vanillaScreen.mouseClicked(mouseX * 256, mouseY * 256, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        advancedMonitor.setClickedThisFrame(false);
                    }
                }
            }
        }
        this.mouseX = (int) (mouseX * 256);
        this.mouseY = (int) (mouseY * 256);
        renderGui(size.getX(), size.getY(), poseStack, buffer, partialTick, (int) (mouseX * 256), (int) (mouseY * 256));
    }
}
