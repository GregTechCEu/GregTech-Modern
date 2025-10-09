package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.base.widget.IVanillaSlot;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.Stencil;
import com.gregtechceu.gtceu.api.mui.overlay.OverlayStack;
import com.gregtechceu.gtceu.api.mui.utils.Animator;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.FpsCounter;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.RichTextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.AbstractContainerScreenAccessor;
import com.gregtechceu.gtceu.core.mixins.client.ScreenAccessor;
import com.gregtechceu.gtceu.integration.xei.handlers.RecipeViewerHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientScreenHandler {

    @Getter
    private static final GuiContext defaultContext = new GuiContext();
    private static final FpsCounter fpsCounter = new FpsCounter();
    private static ModularScreen currentScreen = null;
    @Getter
    private static long ticks = 0L;
    private static IMuiScreen lastMui;

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        Screen newGui = event.getNewScreen();
        defaultContext.reset();

        if (lastMui != null && newGui == null) {
            if (lastMui.getScreen().getPanelManager().isOpen()) {
                lastMui.getScreen().getPanelManager().closeAll();
            }
            lastMui.getScreen().getPanelManager().dispose();
            lastMui = null;
        } else if (newGui instanceof IMuiScreen screenWrapper) {
            if (lastMui == null) {
                lastMui = screenWrapper;
            } else if (lastMui == newGui) {
                lastMui.getScreen().getPanelManager().reopen();
            } else {
                if (lastMui.getScreen().getPanelManager().isOpen()) {
                    lastMui.getScreen().getPanelManager().closeAll();
                }
                lastMui.getScreen().getPanelManager().dispose();
                lastMui = screenWrapper;
            }
        }

        if (newGui instanceof IMuiScreen muiScreen) {
            Objects.requireNonNull(muiScreen.getScreen(), "ModularScreen must not be null!");
            if (currentScreen == muiScreen.getScreen()) {
                currentScreen.getPanelManager().reopen();
            } else {
                if (hasScreen()) {
                    currentScreen.onCloseParent();
                    currentScreen.getPanelManager().dispose();
                }
                currentScreen = muiScreen.getScreen();
                fpsCounter.reset();
            }
        } else if (hasScreen() && event.getCurrentScreen() != null && newGui != event.getCurrentScreen()) {
            currentScreen.onCloseParent();
            currentScreen.getPanelManager().dispose();
            currentScreen = null;
        }
    }

    @SubscribeEvent
    public static void onCloseScreen(ScreenEvent.Closing event) {
        if (hasScreen() && !currentScreen.getPanelManager().isReopened()) {
            currentScreen.onCloseParent();
            currentScreen.getPanelManager().dispose();
            currentScreen = null;
        }
    }

    @SubscribeEvent
    public static void onInitScreenPost(ScreenEvent.Init.Post event) {
        defaultContext.updateScreenArea(event.getScreen().width, event.getScreen().height);
        if (checkGui(event.getScreen())) {
            currentScreen.onResize(event.getScreen().width, event.getScreen().height);
        }
        OverlayStack.foreach(ms -> ms.onResize(event.getScreen().width, event.getScreen().height), false);
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenKeyPressedHigh(ScreenEvent.KeyPressed.Pre event) {
        defaultContext.updateLatestKey(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        keyPressedEvent(event, InputPhase.EARLY);
    }

    // after JEI
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onScreenKeyPressedLow(ScreenEvent.KeyPressed.Pre event) {
        keyPressedEvent(event, InputPhase.LATE);
    }

    private static void keyPressedEvent(ScreenEvent.KeyPressed.Pre event, InputPhase phase) {
        if (checkGui(event.getScreen())) {
            currentScreen.getContext().updateLatestKey(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
        if (handleKeyboardInput(currentScreen, event.getScreen(), true, phase,
                event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenKeyReleasedHigh(ScreenEvent.KeyReleased.Pre event) {
        defaultContext.updateLatestKey(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        keyReleasedEvent(event, InputPhase.EARLY);
    }

    // after JEI
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onScreenKeyReleasedLow(ScreenEvent.KeyReleased.Pre event) {
        keyReleasedEvent(event, InputPhase.LATE);
    }

    private static void keyReleasedEvent(ScreenEvent.KeyReleased.Pre event, InputPhase phase) {
        if (checkGui(event.getScreen())) {
            currentScreen.getContext().updateLatestKey(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
        if (handleKeyboardInput(currentScreen, event.getScreen(), false, phase,
                event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        int button = event.getButton();
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        defaultContext.updateMouseButton(button);
        if (checkGui(event.getScreen())) currentScreen.getContext().updateMouseButton(button);

        if (button == -1) {
            return;
        }
        if (currentScreen != null && currentScreen.handleDraggableInput(mouseX, mouseY, button, true) ||
                doAction(currentScreen, ms -> ms.onMousePressed(mouseX, mouseY, button))) {
            RecipeViewerHandler.getCurrent().setSearchFocused(false);
            event.setCanceled(true);
        }
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        int button = event.getButton();
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        defaultContext.updateMouseButton(button);
        if (checkGui(event.getScreen())) currentScreen.getContext().updateMouseButton(button);

        if (currentScreen != null && currentScreen.handleDraggableInput(mouseX, mouseY, button, false) ||
                doAction(currentScreen, ms -> ms.mouseReleased(mouseX, mouseY, button))) {
            RecipeViewerHandler.getCurrent().setSearchFocused(false);
            event.setCanceled(true);
        }
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        double w = event.getScrollDelta();
        if (w == 0) return;
        defaultContext.updateMouseWheel(w);
        if (checkGui(event.getScreen())) currentScreen.getContext().updateMouseWheel(w);
        checkGui(event.getScreen());
        if (doAction(currentScreen, ms -> ms.mouseScrolled(event.getMouseX(), event.getMouseY(), w))) {
            event.setCanceled(true);
        }
    }

    // before JEI
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        checkGui(event.getScreen());
        if (event.getMouseButton() == -1) {
            return;
        }
        if (doAction(currentScreen, ms -> ms.mouseDragged(event.getMouseX(), event.getMouseY(),
                event.getMouseButton(), event.getDragX(), event.getDragY()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onScreenRenderLow(ScreenEvent.Render.Pre event) {
        int mx = event.getMouseX(), my = event.getMouseY();
        float pt = event.getPartialTick();
        GuiGraphics gc = event.getGuiGraphics();
        defaultContext.setGraphics(gc);
        defaultContext.updateState(mx, my, pt);
        defaultContext.reset();
        if (checkGui(event.getScreen())) {
            currentScreen.getContext().setGraphics(gc);
            currentScreen.getContext().updateState(mx, my, pt);
            drawScreen(gc, currentScreen, currentScreen.getScreenWrapper().getWrappedScreen(), mx, my, pt);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenRenderNormal(ScreenEvent.Render.Post event) {
        OverlayStack.draw(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            OverlayStack.onTick();
            defaultContext.tick();
            if (checkGui()) {
                currentScreen.onUpdate();
            }
            ticks++;
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        }
        Stencil.reset();
    }

    public static void onFrameUpdate() {
        OverlayStack.foreach(ModularScreen::onFrameUpdate, true);
        if (currentScreen != null) currentScreen.onFrameUpdate();
        Animator.advance();
    }

    private static boolean doAction(@Nullable ModularScreen muiScreen, Predicate<ModularScreen> action) {
        return OverlayStack.interact(action, true) || (muiScreen != null && action.test(muiScreen));
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    private static boolean handleKeyboardInput(@Nullable ModularScreen muiScreen, Screen mcScreen,
                                               boolean isPress, InputPhase inputPhase,
                                               int keyCode, int scanCode, int modifiers) {
        if (isPress) {
            // pressing a key
            return inputPhase.isEarly() ? doAction(muiScreen, ms -> ms.keyPressed(keyCode, scanCode, modifiers)) :
                    keyPressed(mcScreen, keyCode, scanCode, modifiers);
        } else {
            // releasing a key
            if (inputPhase.isEarly() && doAction(muiScreen, ms -> ms.keyReleased(keyCode, scanCode, modifiers))) {
                return true;
            }
            if (inputPhase.isLate() && keyCode >= ' ') {
                return keyPressed(mcScreen, keyCode, scanCode, modifiers);
            }
        }
        return false;
    }

    private static boolean keyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (currentScreen == null) return false;
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == 'C' &&
                (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 &&
                (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 &&
                (modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            ConfigHolder.INSTANCE.dev.debugUI = !ConfigHolder.INSTANCE.dev.debugUI;
            return true;
        }
        boolean isInventoryKey = Minecraft.getInstance().options.keyInventory
                .isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
        if (keyCode == 1 || isInventoryKey) {
            if (currentScreen.getContext().hasDraggable()) {
                currentScreen.getContext().dropDraggable();
            } else {
                currentScreen.getPanelManager().closeTopPanel(true);
            }
            return true;
        }
        return false;
    }

    public static void dragSlot(double mouseX, double mouseY, int button, double dragX, double dragY) {
        ModularGuiContext ctx = currentScreen.getContext();
        getMCScreen().mouseDragged(ctx.getMouseX(), ctx.getMouseY(), button, dragX, dragY);
    }

    public static void clickSlot(ModularScreen ms, Slot slot) {
        Screen screen = ms.getScreenWrapper().getWrappedScreen();
        if (screen instanceof ScreenAccessor acc && screen instanceof IClickableContainerScreen clickableScreen &&
                checkGui(screen)) {
            ModularGuiContext ctx = ms.getContext();
            var buttonList = screen.children();
            try {
                // remove buttons to make sure they are not clicked
                acc.setChildren(Collections.emptyList());
                // set clicked slot to make sure the container clicks the desired slot
                clickableScreen.gtceu$setClickedSlot(slot);
                screen.mouseClicked(ctx.getMouseX(), ctx.getMouseY(), ctx.getMouseButton());
            } finally {
                // undo modifications
                clickableScreen.gtceu$setClickedSlot(null);
                acc.setChildren(buttonList);
            }
        }
    }

    public static void releaseSlot() {
        if (hasScreen() && getMCScreen() != null) {
            ModularGuiContext ctx = currentScreen.getContext();
            getMCScreen().mouseReleased(ctx.getMouseX(), ctx.getMouseY(), ctx.getMouseButton());
        }
    }

    public static boolean shouldDrawWorldBackground() {
        return /* ModularUI.isBlurLoaded() || */Minecraft.getInstance().level == null;
    }

    public static void drawDarkBackground(Screen screen, GuiGraphics guiGraphics) {
        if (hasScreen()) {
            float alpha = currentScreen.getMainPanel().getAlpha();
            // vanilla color values as hex
            int color = 0x101010;
            int startAlpha = 0xc0;
            int endAlpha = 0xd0;
            // we need to use normal render type, not overlay here
            guiGraphics.fillGradient(0, 0, screen.width, screen.height,
                    Color.withAlpha(color, (int) (startAlpha * alpha)),
                    Color.withAlpha(color, (int) (endAlpha * alpha)));
            MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(screen, guiGraphics));
        }
    }

    public static void drawScreen(GuiGraphics graphics, ModularScreen muiScreen, Screen mcScreen, int mouseX,
                                  int mouseY, float partialTicks) {
        if (mcScreen instanceof AbstractContainerScreen<?> container) {
            drawContainer(graphics, muiScreen, container, mouseX, mouseY, partialTicks);
        } else {
            drawScreenInternal(graphics, muiScreen, mcScreen, mouseX, mouseY, partialTicks);
        }
    }

    public static void drawScreenInternal(GuiGraphics graphics, ModularScreen muiScreen, Screen mcScreen, int mouseX,
                                          int mouseY, float partialTicks) {
        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        muiScreen.render(graphics, mouseX, mouseY, partialTicks);
        RenderSystem.disableDepthTest();
        drawVanillaElements(graphics, mcScreen, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Lighting.setupForFlatItems();
        muiScreen.drawForeground(graphics, partialTicks);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        Stencil.remove();
    }

    public static void drawContainer(GuiGraphics graphics, ModularScreen muiScreen, AbstractContainerScreen<?> mcScreen,
                                     int mouseX, int mouseY, float partialTicks) {
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) mcScreen;

        Stencil.reset();
        Stencil.apply(muiScreen.getScreenArea(), null);
        mcScreen.renderBackground(graphics);
        int x = mcScreen.getGuiLeft();
        int y = mcScreen.getGuiTop();

        acc.invokeRenderBg(graphics, partialTicks, mouseX, mouseY);
        muiScreen.render(graphics, mouseX, mouseY, partialTicks);

        RenderSystem.disableDepthTest();
        // mainly for invtweaks compat
        drawVanillaElements(graphics, mcScreen, mouseX, mouseY, partialTicks);
        graphics.pose().pushPose();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        acc.setHoveredSlot(null);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        Lighting.setupForFlatItems();
        acc.invokeRenderLabels(graphics, mouseX, mouseY);
        muiScreen.drawForeground(graphics, partialTicks);
        Lighting.setupFor3DItems();

        acc.setHoveredSlot(null);
        IGuiElement hovered = muiScreen.getContext().getHovered();
        if (hovered instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
            acc.setHoveredSlot(vanillaSlot.getVanillaSlot());
        }

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(mcScreen, graphics, mouseX, mouseY));

        AbstractContainerMenu menu = mcScreen.getMenu();
        ItemStack draggingItem = acc.getDraggingItem().isEmpty() ? menu.getCarried() : acc.getDraggingItem();
        if (!draggingItem.isEmpty()) {
            int xOffset = 8;
            int yOffset = acc.getDraggingItem().isEmpty() ? 8 : 16;
            String text = null;

            if (!acc.getDraggingItem().isEmpty() && acc.getIsSplittingStack()) {
                draggingItem = draggingItem.copyWithCount(Mth.ceil(draggingItem.getCount() / 2.0F));
            } else if (acc.getIsQuickCrafting() && acc.getQuickCraftSlots().size() > 1) {
                draggingItem = draggingItem.copyWithCount(acc.getQuickCraftingRemainder());
                if (draggingItem.isEmpty()) {
                    text = ChatFormatting.YELLOW + "0";
                }
            }

            drawFloatingItemStack(mcScreen, graphics, draggingItem, mouseX - x - xOffset, mouseY - y - yOffset, text);
        }
        graphics.pose().popPose();

        if (!acc.getSnapbackItem().isEmpty()) {
            float delta = (float) (Util.getMillis() - acc.getSnapbackTime()) / 100.0F;

            if (delta >= 1.0F) {
                delta = 1.0F;
                acc.setSnapbackItem(ItemStack.EMPTY);
            }

            int snapBackOffsetX = acc.getSnapbackEnd().x - acc.getSnapbackStartX();
            int snapBackOffsetY = acc.getSnapbackEnd().y - acc.getSnapbackStartY();
            int snapBackX = acc.getSnapbackStartX() + (int) ((float) snapBackOffsetX * delta);
            int snapBackY = acc.getSnapbackStartY() + (int) ((float) snapBackOffsetY * delta);
            drawFloatingItemStack(mcScreen, graphics, acc.getSnapbackItem(), snapBackX, snapBackY, null);
        }
        graphics.pose().popPose();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        Stencil.remove();
    }

    private static void drawFloatingItemStack(AbstractContainerScreen<?> mcScreen, GuiGraphics graphics,
                                              ItemStack stack, int x, int y, String altText) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 232.0F);

        var font = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font == null) font = ((ScreenAccessor) mcScreen).getFont();
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack,
                x, y - (((AbstractContainerScreenAccessor) mcScreen).getDraggingItem().isEmpty() ? 0 : 8), altText);
        graphics.pose().popPose();
    }

    private static void drawVanillaElements(GuiGraphics graphics, Screen mcScreen, int mouseX, int mouseY,
                                            float partialTicks) {
        for (Renderable renderable : mcScreen.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    public static void drawDebugScreen(GuiGraphics graphics, @Nullable ModularScreen muiScreen,
                                       @Nullable ModularScreen fallback) {
        fpsCounter.onDraw();
        if (!ConfigHolder.INSTANCE.dev.debugUI) return;
        if (muiScreen == null) {
            if (checkGui()) {
                muiScreen = currentScreen;
            } else {
                if (fallback == null) return;
                muiScreen = fallback;
            }
        }
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        ModularGuiContext context = muiScreen.getContext();
        Matrix4f pose = graphics.pose().last().pose();

        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenH = muiScreen.getScreenArea().height;
        int color = Color.argb(180, 40, 115, 220);
        float scale = 0.80f;
        int shift = (int) (11 * scale + 0.5f);
        int lineY = screenH - shift - 2;
        GuiDraw.drawText(graphics, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, scale, color, true);
        lineY -= shift;
        GuiDraw.drawText(graphics, "FPS: " + fpsCounter.getFps(), 5, screenH - 24, scale, color, true);
        LocatedWidget locatedHovered = muiScreen.getPanelManager().getTopWidgetLocated(true);
        if (locatedHovered != null) {
            drawSegmentLine(graphics, lineY -= 4, scale, color);
            lineY -= 10;

            IGuiElement hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            graphics.pose().pushPose();
            context.applyTo(graphics.pose());

            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(graphics, 0, 0, area.width, area.height, color, scale);
            if (hovered.hasParent()) {
                GuiDraw.drawBorder(graphics, -area.rx, -area.ry, parent.getArea().width, parent.getArea().height,
                        Color.withAlpha(color, 0.3f), scale);
            }
            graphics.pose().popPose();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText(graphics, "Pos: " + area.x + ", " + area.y + "  Rel: " + area.rx + ", " + area.ry, 5,
                    lineY, scale, color, false);
            lineY -= shift;
            GuiDraw.drawText(graphics, "Size: " + area.width + ", " + area.height, 5, lineY, scale, color, true);
            lineY -= shift;
            GuiDraw.drawText(graphics, "Class: " + hovered, 5, lineY, 1, color, true);
            if (hovered.hasParent()) {
                drawSegmentLine(graphics, lineY -= 4, scale, color);
                lineY -= 10;
                area = parent.getArea();
                GuiDraw.drawText(graphics, "Parent size: " + area.width + ", " + area.height, 5, lineY, scale, color,
                        true);
                lineY -= shift;
                GuiDraw.drawText(graphics, "Parent: " + parent, 5, lineY, 1, color, true);
            }
            if (hovered instanceof ItemSlot slotWidget) {
                drawSegmentLine(graphics, lineY -= 4, scale, color);
                lineY -= 10;
                ModularSlot slot = slotWidget.getSlot();
                GuiDraw.drawText(graphics, "Slot Index: " + slot.getSlotIndex(), 5, lineY, scale, color, false);
                lineY -= shift;
                GuiDraw.drawText(graphics, "Slot Number: " + ((Slot) slot).index, 5, lineY, scale, color, false);
                lineY -= shift;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = slot.getSlotGroup();
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.isAllowShiftTransfer();
                    GuiDraw.drawText(graphics,
                            "Shift-Click Priority: " +
                                    (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"),
                            5, lineY, scale, color, true);
                }
            } else if (hovered instanceof RichTextWidget richTextWidget) {
                drawSegmentLine(graphics, lineY -= 4, scale, color);
                lineY -= 10;
                Object hoveredElement = richTextWidget.getHoveredElement();
                GuiDraw.drawText(graphics, "Hovered: " + hoveredElement, 5, lineY, scale, color, true);
            }
        }
        // dot at mouse pos
        GuiDraw.drawRect(graphics, mouseX, mouseY, 1, 1, Color.withAlpha(Color.GREEN.main, 0.8f));
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private static void drawSegmentLine(GuiGraphics graphics, int y, float scale, int color) {
        GuiDraw.drawRect(graphics, 5, y, 140 * scale, 1 * scale, color);
    }

    public static boolean hasScreen() {
        return currentScreen != null;
    }

    @Nullable
    public static Screen getMCScreen() {
        return MCHelper.getCurrentScreen();
    }

    @Nullable
    public static ModularScreen getMuiScreen() {
        return currentScreen;
    }

    private static boolean checkGui() {
        return checkGui(MCHelper.getCurrentScreen());
    }

    private static boolean checkGui(Screen screen) {
        if (currentScreen == null || !(screen instanceof IMuiScreen muiScreen)) return false;
        if (screen != MCHelper.getCurrentScreen() || muiScreen.getScreen() != currentScreen) {
            defaultContext.reset();
            currentScreen = null;
            return false;
        }
        return true;
    }

    public static GuiContext getBestContext() {
        if (checkGui()) {
            return currentScreen.getContext();
        }
        return defaultContext;
    }

    private enum InputPhase {

        // for mui interactions
        EARLY,
        // for mc interactions (like E and ESC)
        LATE;

        public boolean isEarly() {
            return this == EARLY;
        }

        public boolean isLate() {
            return this == LATE;
        }
    }
}
