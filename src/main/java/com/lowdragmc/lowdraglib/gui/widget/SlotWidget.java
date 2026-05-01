package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SlotWidget extends Widget implements IRecipeIngredientSlot {

    public static final ResourceBorderTexture ITEM_SLOT_TEXTURE = ResourceBorderTexture.BUTTON_COMMON;
    public static Slot HOVER_SLOT;

    public boolean isPlayerContainer;
    public boolean isPlayerHotBar;
    public boolean drawHoverOverlay = true;
    public boolean drawHoverTips = true;
    protected boolean canTakeItems = true;
    protected boolean canPutItems = true;
    protected Slot slotReference;
    protected IngredientIO ingredientIO = IngredientIO.RENDER_ONLY;
    protected float XEIChance = 1.0f;
    protected Runnable changeListener;
    protected BiConsumer<SlotWidget, List<Component>> onAddedTooltips;
    protected Function<ItemStack, ItemStack> itemHook = Function.identity();
    protected ItemStack lastItem = ItemStack.EMPTY;

    public SlotWidget() {
        super(0, 0, 18, 18);
        setBackgroundTexture(ITEM_SLOT_TEXTURE);
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems) {
        this();
        setSelfPosition(xPosition, yPosition);
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        setContainerSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                      boolean canTakeItems, boolean canPutItems) {
        this();
        setSelfPosition(xPosition, yPosition);
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        setHandlerSlot(itemHandler, slotIndex);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        this(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    public void initTemplate() {}

    public SlotWidget setContainerSlot(Container inventory, int slotIndex) {
        slotReference = new Slot(inventory, slotIndex, getPositionX(), getPositionY());
        return this;
    }

    public SlotWidget setHandlerSlot(IItemHandlerModifiable itemHandler, int slotIndex) {
        slotReference = new Slot(new SimpleContainer(0), slotIndex, getPositionX(), getPositionY()) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return canPutStack(stack) && itemHandler.isItemValid(slotIndex, stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return canTakeStack(player);
            }

            @Override
            public ItemStack getItem() {
                return itemHandler.getStackInSlot(slotIndex);
            }

            @Override
            public void set(ItemStack stack) {
                itemHandler.setStackInSlot(slotIndex, stack);
                setChanged();
            }

            @Override
            public ItemStack remove(int amount) {
                return itemHandler.extractItem(slotIndex, amount, false);
            }
        };
        return this;
    }

    public void updateSlot(Slot slot) {
        this.slotReference = slot;
    }

    public ItemStack getItem() {
        return slotReference == null ? ItemStack.EMPTY : slotReference.getItem();
    }

    public void setItem(ItemStack stack) {
        setItem(stack, true);
    }

    public void setItem(ItemStack stack, boolean notify) {
        if (slotReference != null) {
            slotReference.set(stack);
        }
        if (notify) onSlotChanged();
    }

    @Override
    public final void setSize(Size size) {
        super.setSize(size);
    }

    @Override
    public void setGui(com.lowdragmc.lowdraglib.gui.modular.ModularUI gui) {
        super.setGui(gui);
        if (gui != null && slotReference != null) {
            gui.getModularUIContainer().addSlot(slotReference);
        }
    }

    public SlotWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture == null ? IGuiTexture.EMPTY : backgroundTexture;
        setBackground(backgroundTexture);
        return this;
    }

    public boolean canPutStack(ItemStack stack) {
        return canPutItems;
    }

    public boolean canTakeStack(Player player) {
        return canTakeItems;
    }

    public boolean isEnabled() {
        return isActive();
    }

    public boolean canMergeSlot(ItemStack stack) {
        return true;
    }

    public void onSlotChanged() {
        if (changeListener != null) changeListener.run();
    }

    public ItemStack slotClick(int mouseButton, ContainerInput clickType, Player player) {
        return ItemStack.EMPTY;
    }

    public final Slot getHandler() {
        return slotReference;
    }

    public SlotWidget setLocationInfo(boolean isPlayerContainer, boolean isPlayerHotBar) {
        this.isPlayerContainer = isPlayerContainer;
        this.isPlayerHotBar = isPlayerHotBar;
        return this;
    }

    @Override
    public List<Component> getTooltipTexts() {
        List<Component> tooltips = super.getTooltipTexts();
        if (onAddedTooltips != null) {
            onAddedTooltips.accept(this, tooltips);
        }
        return tooltips;
    }

    public List<Component> getAdditionalToolTips(List<Component> tooltips) {
        return tooltips;
    }

    @Override
    public List<Component> getFullTooltipTexts() {
        return getTooltipTexts();
    }

    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        return isMouseOverElement(mouseX, mouseY) ? getXEICurrentIngredient() : null;
    }

    @Override
    public List<Object> getXEIIngredients() {
        ItemStack item = getItem();
        return item.isEmpty() ? List.of() : List.of(getRealStack(item));
    }

    public ItemStack getRealStack(ItemStack stack) {
        return itemHook.apply(stack);
    }

    @Override
    public Object getXEICurrentIngredient() {
        return getItem();
    }

    public SlotWidget setCanTakeItems(boolean canTakeItems) {
        this.canTakeItems = canTakeItems;
        return this;
    }

    public SlotWidget setCanPutItems(boolean canPutItems) {
        this.canPutItems = canPutItems;
        return this;
    }

    public SlotWidget setDrawHoverOverlay(boolean drawHoverOverlay) {
        this.drawHoverOverlay = drawHoverOverlay;
        return this;
    }

    public SlotWidget setDrawHoverTips(boolean drawHoverTips) {
        this.drawHoverTips = drawHoverTips;
        return this;
    }

    public SlotWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public SlotWidget setOnAddedTooltips(BiConsumer<SlotWidget, List<Component>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    public SlotWidget setItemHook(Function<ItemStack, ItemStack> itemHook) {
        this.itemHook = itemHook;
        return this;
    }

    public SlotWidget setIngredientIO(IngredientIO ingredientIO) {
        this.ingredientIO = ingredientIO;
        return this;
    }

    @Override
    public IngredientIO getIngredientIO() {
        return ingredientIO;
    }

    public SlotWidget setXEIChance(float XEIChance) {
        this.XEIChance = XEIChance;
        return this;
    }

    @Override
    public float getXEIChance() {
        return XEIChance;
    }

    public ItemStack getLastItem() {
        return lastItem;
    }
}
