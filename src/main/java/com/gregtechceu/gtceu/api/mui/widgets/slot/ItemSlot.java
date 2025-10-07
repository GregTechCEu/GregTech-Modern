package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.widget.IVanillaSlot;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetSlotTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.core.mixins.client.AbstractContainerScreenAccessor;
import com.gregtechceu.gtceu.core.mixins.client.ScreenAccessor;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, IngredientProvider<ItemStack> {

    public static final int SIZE = 18;

    public static ItemSlot create(boolean phantom) {
        return phantom ? new PhantomItemSlot() : new ItemSlot();
    }

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSH syncHandler;
    @Setter
    protected UnaryOperator<ItemStack> itemHook;

    public ItemSlot() {
        tooltip().setAutoUpdate(true);// .setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = getSlot().getItem();
            buildTooltip(stack, tooltip);
        });
    }

    @Override
    public void onInit() {
        if (getScreen().isOverlay()) {
            throw new IllegalStateException("Overlays can't have slots!");
        }
        size(SIZE);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, ItemSlotSH.class);
        return this.syncHandler != null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean shouldBeEnabled = areAncestorsEnabled();
        if (shouldBeEnabled != getSlot().isActive()) {
            this.syncHandler.setEnabled(shouldBeEnabled, true);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.syncHandler == null) return;
        Lighting.setupFor3DItems();
        drawSlot(context, getSlot());
        Lighting.setupFor3DItems();
        drawOverlay(context);
    }

    protected void drawOverlay(ModularGuiContext context) {
        if (isHovering()) {
            RenderSystem.colorMask(true, true, true, false);
            GuiDraw.drawRect(context.getGraphics(), 1, 1, 16, 16, getSlotHoverColor());
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(context, getSlot().getItem());
        }
    }

    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack.isEmpty()) return;
        tooltip.addFromItem(stack);
    }

    @Override
    public WidgetSlotTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getItemSlotTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        ClientScreenHandler.clickSlot(getScreen(), getSlot());
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        ClientScreenHandler.releaseSlot();
        return true;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        ClientScreenHandler.dragSlot(mouseX, mouseY, button, dragX, dragY);
    }

    public ModularSlot getSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return true;
    }

    @Override
    public @NotNull ItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    public ItemSlot slot(ModularSlot slot) {
        this.syncHandler = new ItemSlotSH(slot);
        setSyncHandler(this.syncHandler);
        return this;
    }

    public ItemSlot slot(IItemHandlerModifiable itemHandler, int index) {
        return slot(new ModularSlot(itemHandler, index));
    }

    @OnlyIn(Dist.CLIENT)
    private void drawSlot(ModularGuiContext context, Slot slotIn) {
        Screen guiScreen = getScreen().getScreenWrapper().getWrappedScreen();
        if (!(guiScreen instanceof AbstractContainerScreen<?>))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) guiScreen;
        ItemStack slotStack = slotIn.getItem();
        boolean isDragPreview = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && !acc.getDraggingItem().isEmpty() &&
                !acc.getIsSplittingStack();
        ItemStack carried = guiScreen.getMinecraft().player.containerMenu.getCarried();
        int amount = -1;
        String format = null;

        if (!getSyncHandler().isPhantom()) {
            if (slotIn == acc.getClickedSlot() && !acc.getDraggingItem().isEmpty() && acc.getIsSplittingStack() &&
                    !slotStack.isEmpty()) {
                slotStack = slotStack.copy();
                slotStack.setCount(slotStack.getCount() / 2);
            } else if (acc.getIsQuickCrafting() && acc.getQuickCraftSlots().contains(slotIn) && !carried.isEmpty()) {
                if (acc.getQuickCraftSlots().size() == 1) {
                    return;
                }

                if (AbstractContainerMenu.canItemQuickReplace(slotIn, carried, true) &&
                        getScreen().getContainer().canDragTo(slotIn)) {
                    slotStack = carried.copy();
                    isDragPreview = true;
                    AbstractContainerMenu.getQuickCraftPlaceCount(acc.getQuickCraftSlots(), acc.getQuickCraftingType(),
                            slotStack);
                    int k = Math.min(slotStack.getMaxStackSize(), slotIn.getMaxStackSize(slotStack));

                    if (slotStack.getCount() > k) {
                        amount = k;
                        format = ChatFormatting.YELLOW.toString();
                        slotStack.setCount(k);
                    }
                } else {
                    acc.getQuickCraftSlots().remove(slotIn);
                    acc.invokeRecalculateQuickCraftRemaining();
                }
            }
        }

        // makes sure items of different layers don't interfere with each other visually
        float z = context.getCurrentDrawingZ() + 100;
        context.getGraphics().pose().pushPose();
        context.getGraphics().pose().translate(0, 0, z);

        if (!flag1) {
            if (isDragPreview) {
                GuiDraw.drawRect(context.getGraphics(), 1, 1, 16, 16, -2130706433);
            }

            if (!slotStack.isEmpty()) {
                RenderSystem.enableDepthTest();
                // render the item itself

                context.getGraphics().renderItem(slotStack, 1, 1);
                if (amount < 0) {
                    amount = slotStack.getCount();
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    String amountText = FormattingUtil.formatNumberReadable(amount, false);
                    if (format != null) {
                        amountText = format + amountText;
                    }
                    float scale = 1f;
                    if (amountText.length() == 3) {
                        scale = 0.8f;
                    } else if (amountText.length() == 4) {
                        scale = 0.6f;
                    } else if (amountText.length() > 4) {
                        scale = 0.5f;
                    }
                    textRenderer.setShadow(true);
                    textRenderer.setScale(scale);
                    textRenderer.setColor(Color.WHITE.main);
                    textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
                    textRenderer.setPos(1, 1);
                    RenderSystem.disableDepthTest();
                    RenderSystem.disableBlend();
                    context.getGraphics().pose().translate(0, 0, 100 + z);
                    textRenderer.draw(context.getGraphics(), amountText);
                    RenderSystem.enableDepthTest();
                    RenderSystem.enableBlend();
                }

                int cachedCount = slotStack.getCount();
                slotStack.setCount(1); // required to not render the amount overlay
                // render other overlays like durability bar
                context.getGraphics().renderItemDecorations(((ScreenAccessor) guiScreen).getFont(), slotStack, 1, 1,
                        null);
                slotStack.setCount(cachedCount);
                RenderSystem.disableDepthTest();
            }
        }
        context.getGraphics().pose().popPose();
    }

    @Override
    public ItemStackList getIngredients() {
        return ItemStackList.of(this.syncHandler.getSlot().getItem());
    }

    @Override
    public @NotNull Class<ItemStack> ingredientClass() {
        return ItemStack.class;
    }

    @Override
    public UnaryOperator<ItemStack> renderMappingFunction() {
        return this.itemHook;
    }
}
