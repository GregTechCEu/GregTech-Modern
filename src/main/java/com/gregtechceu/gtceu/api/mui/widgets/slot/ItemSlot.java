package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.value.ISyncOrValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IVanillaSlot;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.SlotTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.core.mixins.client.AbstractContainerScreenAccessor;
import com.gregtechceu.gtceu.core.mixins.client.ScreenAccessor;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;

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
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, IngredientProvider<ItemStack> {

    public static final int SIZE = 18;

    public static ItemSlot create(boolean phantom) {
        return phantom ? new PhantomItemSlot() : new ItemSlot();
    }

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSyncHandler syncHandler;
    private RichTooltip tooltip;
    @Setter
    protected UnaryOperator<ItemStack> itemHook;

    public ItemSlot() {
        itemTooltip().autoUpdate(true);// .setHasTitleMargin(true);
        itemTooltip().tooltipBuilder(tooltip -> {
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
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof ItemSlotSyncHandler;
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(ItemSlotSyncHandler.class);
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
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.syncHandler == null) return;
        Lighting.setupFor3DItems();
        drawSlot(context, getSlot());
        drawOverlay(context);
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        drawOverlay(context);
    }

    protected void drawOverlay(ModularGuiContext context) {
        if (isHovering()) {
            GuiDraw.drawRect(context.getGraphics(), 1, 1, 16, 16, getSlotHoverColor());
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.showUpTimer())) {
            tooltip.draw(context, getSlot().getItem());
        }
    }

    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack.isEmpty()) return;
        tooltip.addFromItem(stack);
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        PlayerSlotType playerSlotType = this.syncHandler != null ? this.syncHandler.getPlayerSlotType() : null;
        if (playerSlotType == null) return theme.getWidgetTheme(IThemeApi.ITEM_SLOT);
        return switch (playerSlotType) {
            case HOTBAR -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_HOTBAR);
            case MAIN_INVENTORY -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_MAIN_INV);
            case OFFHAND -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_OFFHAND);
            case ARMOR -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_ARMOR);
        };
    }

    public int getSlotHoverColor() {
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getContext().getTheme(), SlotTheme.class);
        return theme.getTheme().getSlotHoverColor();
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
    public @NotNull ItemSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    public RichTooltip getItemTooltip() {
        return super.getTooltip();
    }

    public RichTooltip itemTooltip() {
        return super.tooltip();
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        if (isSynced() && !getSlot().getItem().isEmpty()) {
            return getItemTooltip();
        }
        return tooltip;
    }

    @Override
    public ItemSlot tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new RichTooltip().parent(this);
        }
        return this.tooltip;
    }

    public ItemSlot slot(ModularSlot slot) {
        return syncHandler(new ItemSlotSyncHandler(slot));
    }

    public ItemSlot slot(IItemHandlerModifiable itemHandler, int index) {
        return slot(new ModularSlot(itemHandler, index));
    }

    public ItemSlot syncHandler(ItemSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    private void drawSlot(ModularGuiContext context, ModularSlot slotIn) {
        // TODO: NEA animations
        Screen guiScreen = getScreen().getScreenWrapper().getWrappedScreen();
        if (!(guiScreen instanceof AbstractContainerScreen<?>))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) guiScreen;
        ItemStack slotStack = slotIn.getItem();
        boolean isDragPreview = false;
        boolean doDrawItem = slotIn == acc.getClickedSlot() && !acc.getDraggingItem().isEmpty() &&
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
        context.graphicsPose().pushPose();
        context.graphicsPose().translate(0, 0, z);

        if (!doDrawItem) {
            if (isDragPreview) {
                GuiDraw.drawRect(context.getGraphics(), 1, 1, 16, 16, 0x80FFFFFF);
            }

            if (!slotStack.isEmpty()) {
                RenderSystem.enableDepthTest();
                // render the item itself

                context.getGraphics().renderItem(slotStack, 1, 1);
                if (amount < 0) {
                    amount = slotStack.getCount();
                }
                GuiDraw.drawStandardSlotAmountText(context, amount, format, getArea(), z);

                int cachedCount = slotStack.getCount();
                slotStack.setCount(1); // required to not render the amount overlay
                // render other overlays like durability bar
                context.getGraphics().renderItemDecorations(((ScreenAccessor) guiScreen).getFont(), slotStack, 1, 1,
                        null);
                slotStack.setCount(cachedCount);
                RenderSystem.disableDepthTest();
            }
        }
        context.graphicsPose().popPose();
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
