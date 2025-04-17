package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemStackHandler;

import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.runtime.ConfiguratorParser;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LDLRegister(name = "gtm_item_slot", group = "widget.gtm_container", priority = 50)
public class SlotWidget extends com.lowdragmc.lowdraglib.gui.widget.SlotWidget {

    public SlotWidget() {
        super();
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                      boolean canTakeItems, boolean canPutItems) {
        this.setSelfPosition(xPosition, yPosition);
        this.setSize(18, 18);
        this.recomputePosition();
        this.setVisible(true);
        this.setActive(true);
        this.drawHoverOverlay = true;
        this.drawHoverTips = true;
        this.ingredientIO = IngredientIO.RENDER_ONLY;
        this.XEIChance = 1.0f;
        this.setBackgroundTexture(ITEM_SLOT_TEXTURE);
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.setHandlerSlot(itemHandler, slotIndex);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(Container container, int slotIndex, int xPosition, int yPosition) {
        this(container, slotIndex, xPosition, yPosition, true, true);
    }

    protected Slot createSlot(IItemHandlerModifiable itemHandler, int index) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0);
    }

    public SlotWidget setContainerSlot(Container inventory, int slotIndex) {
        super.setContainerSlot(inventory, slotIndex);
        return this;
    }

    @Override
    public void updateSlot(Slot slot) {
        super.updateSlot(slot);
    }

    @Override
    public SlotWidget setHandlerSlot(IItemTransfer itemHandler, int slotIndex) {
        super.setHandlerSlot(itemHandler, slotIndex);
        return this;
    }

    public SlotWidget setHandlerSlot(IItemHandlerModifiable itemHandler, int slotIndex) {
        updateSlot(createSlot(itemHandler, slotIndex));
        return this;
    }

    @Override
    public SlotWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        super.setBackgroundTexture(backgroundTexture);
        return this;
    }

    @Override
    public SlotWidget setLocationInfo(boolean isPlayerContainer, boolean isPlayerHotBar) {
        super.setLocationInfo(isPlayerContainer, isPlayerHotBar);
        return this;
    }

    @Override
    public SlotWidget setCanTakeItems(boolean canTakeItems) {
        super.setCanTakeItems(canTakeItems);
        return this;
    }

    @Override
    public SlotWidget setCanPutItems(boolean canPutItems) {
        super.setCanPutItems(canPutItems);
        return this;
    }

    @Override
    public SlotWidget setDrawHoverOverlay(boolean drawHoverOverlay) {
        super.setDrawHoverOverlay(drawHoverOverlay);
        return this;
    }

    @Override
    public SlotWidget setDrawHoverTips(boolean drawHoverTips) {
        super.setDrawHoverTips(drawHoverTips);
        return this;
    }

    @Override
    public SlotWidget setIngredientIO(IngredientIO ingredientIO) {
        super.setIngredientIO(ingredientIO);
        return this;
    }

    @Override
    public SlotWidget setChangeListener(Runnable changeListener) {
        super.setChangeListener(changeListener);
        return this;
    }

    @Override
    public SlotWidget setXEIChance(float XEIChance) {
        super.setXEIChance(XEIChance);
        return this;
    }

    @Override
    public SlotWidget setItemHook(Function<ItemStack, ItemStack> itemHook) {
        super.setItemHook(itemHook);
        return this;
    }

    @Override
    public SlotWidget setOnAddedTooltips(BiConsumer<com.lowdragmc.lowdraglib.gui.widget.SlotWidget, List<Component>> onAddedTooltips) {
        super.setOnAddedTooltips(onAddedTooltips);
        return this;
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        var handler = new ItemStackHandler();
        handler.setStackInSlot(0, Blocks.STONE.asItem().getDefaultInstance());
        father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview", new SlotWidget() {

            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(SlotWidget.this.tooltipTexts);
                this.backgroundTexture = SlotWidget.this.backgroundTexture;
                this.hoverTexture = SlotWidget.this.hoverTexture;
                this.drawHoverOverlay = SlotWidget.this.drawHoverOverlay;
                this.drawHoverTips = SlotWidget.this.drawHoverTips;
                this.overlay = SlotWidget.this.overlay;
            }
        }.setCanPutItems(false).setCanTakeItems(false).setHandlerSlot(handler, 0)));

        ConfiguratorParser.createConfigurators(father, new HashMap<>(), getClass(), this);
    }

    @Nullable
    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (self().isMouseOverElement(mouseX, mouseY)) {
            var handler = getHandler();
            if (handler == null) return null;
            ItemStack realStack = getRealStack(handler.getItem());
            if (handler instanceof WidgetSlotItemHandler slotHandler) {
                if (slotHandler.itemHandler instanceof CycleItemStackHandler stackHandler) {
                    return getXEIIngredientsClickable(stackHandler, slotHandler.index);
                } else if (slotHandler.itemHandler instanceof CycleItemEntryHandler entryHandler) {
                    return getXEIIngredientsClickable(entryHandler, slotHandler.index);
                }
            }

            if (GTCEu.Mods.isJEILoaded() && !realStack.isEmpty()) {
                return JEICallWrapper.getJEIStackClickable(realStack, getPosition(), getSize());
            } else if (GTCEu.Mods.isREILoaded()) {
                return EntryStacks.of(realStack);
            } else if (GTCEu.Mods.isEMILoaded()) {
                return EmiStack.of(realStack).setChance(getXEIChance());
            }
            return realStack;
        }
        return null;
    }

    @Override
    public List<Object> getXEIIngredients() {
        if (slotReference == null || slotReference.getItem().isEmpty()) return Collections.emptyList();
        var handler = getHandler();
        if (handler == null) return Collections.emptyList();
        ItemStack realStack = getRealStack(handler.getItem());
        if (handler instanceof WidgetSlotItemHandler slotHandler) {
            if (slotHandler.itemHandler instanceof CycleItemStackHandler stackHandler) {
                return getXEIIngredientsClickable(stackHandler, slotHandler.index);
            } else if (slotHandler.itemHandler instanceof CycleItemEntryHandler entryHandler) {
                return getXEIIngredientsClickable(entryHandler, slotHandler.index);
            }
        }

        if (GTCEu.Mods.isJEILoaded() && !realStack.isEmpty()) {
            return List.of(JEICallWrapper.getJEIStackClickable(realStack, getPosition(), getSize()));
        } else if (GTCEu.Mods.isREILoaded()) {
            return List.of(EntryStacks.of(realStack));
        } else if (GTCEu.Mods.isEMILoaded()) {
            return List.of(EmiStack.of(realStack).setChance(getXEIChance()));
        }
        return List.of(realStack);
    }

    private List<Object> getXEIIngredients(CycleItemStackHandler handler, int index) {
        var stackList = handler.getStackList(index);
        if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIIngredients(stackList, this::getRealStack);
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredients(stackList, this::getRealStack);
        } else if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(stackList, getXEIChance(), this::getRealStack);
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsClickable(CycleItemStackHandler handler, int index) {
        var stackList = handler.getStackList(index);
        if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIIngredientsClickable(stackList, getPosition(), getSize(), this::getRealStack);
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredients(stackList, this::getRealStack);
        } else if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(stackList, getXEIChance(), this::getRealStack);
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredients(CycleItemEntryHandler handler, int index) {
        ItemEntryList entryList = handler.getEntry(index);
        if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIIngredients(entryList, this::getRealStack);
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredients(entryList, this::getRealStack);
        } else if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(entryList, getXEIChance(), this::getRealStack);
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsClickable(CycleItemEntryHandler handler, int index) {
        ItemEntryList entryList = handler.getEntry(index);
        if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIIngredientsClickable(entryList, getPosition(), getSize(), this::getRealStack);
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredients(entryList, this::getRealStack);
        } else if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(entryList, getXEIChance(), this::getRealStack);
        }
        return Collections.emptyList();
    }

    public class WidgetSlotItemHandler extends Slot {

        private static final Container emptyInventory = new SimpleContainer(0);
        @Getter
        private final IItemHandlerModifiable itemHandler;
        private final int index;

        public WidgetSlotItemHandler(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
            super(emptyInventory, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
            this.index = index;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) &&
                    (!stack.isEmpty() && this.itemHandler.isItemValid(this.index, stack));
        }

        @Override
        public boolean mayPickup(@Nullable Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && !this.itemHandler.extractItem(index, 1, true).isEmpty();
        }

        @Override
        @NotNull
        public ItemStack getItem() {
            return this.itemHandler.getStackInSlot(index);
        }

        @Override
        public void setByPlayer(@NotNull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
            this.setChanged();
        }

        @Override
        public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {}

        @Override
        public int getMaxStackSize() {
            return this.itemHandler.getSlotLimit(this.index);
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            ItemStack maxAdd = stack.copy();
            int maxInput = stack.getMaxStackSize();
            maxAdd.setCount(maxInput);
            ItemStack currentStack = this.itemHandler.getStackInSlot(index);
            this.itemHandler.setStackInSlot(index, ItemStack.EMPTY);
            ItemStack remainder = this.itemHandler.insertItem(index, maxAdd, true);
            this.itemHandler.setStackInSlot(index, currentStack);
            return maxInput - remainder.getCount();
        }

        @NotNull
        @Override
        public ItemStack remove(int amount) {
            var result = this.itemHandler.extractItem(index, amount, false);
            if (changeListener != null && !getItem().isEmpty()) {
                changeListener.run();
            }
            return result;
        }

        @Override
        public void setChanged() {
            if (changeListener != null) {
                changeListener.run();
            }
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isEnabled() && (HOVER_SLOT == null || HOVER_SLOT == this);
        }
    }

    public static final class JEICallWrapper {

        public static Object getJEIStackClickable(ItemStack stack, Position pos, Size size) {
            return JEIPlugin.getItemIngredient(stack, pos.x, pos.y, size.width, size.height);
        }

        public static List<Object> getJEIIngredients(ItemEntryList list, UnaryOperator<ItemStack> realStack) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(realStack)
                    .collect(Collectors.toList());
        }

        public static List<Object> getJEIIngredientsClickable(ItemEntryList list, Position pos, Size size,
                                                              UnaryOperator<ItemStack> realStack) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(realStack)
                    .map(stack -> getJEIStackClickable(stack, pos, size))
                    .collect(Collectors.toList());
        }
    }

    public static final class REICallWrapper {

        private static EntryIngredient toREIIngredient(Stream<ItemStack> stream, UnaryOperator<ItemStack> realStack) {
            return EntryIngredient.of(stream.map(realStack)
                    .map(EntryStacks::of)
                    .toList());
        }

        public static List<Object> getREIIngredients(ItemStackList list, UnaryOperator<ItemStack> realStack) {
            return List.of(toREIIngredient(list.stream(), realStack));
        }

        public static List<Object> getREIIngredients(ItemTagList list, UnaryOperator<ItemStack> realStack) {
            return list.getEntries().stream()
                    .map(ItemTagList.ItemTagEntry::stacks)
                    .map(stream -> toREIIngredient(stream, realStack))
                    .collect(Collectors.toList());
        }

        public static List<Object> getREIIngredients(ItemEntryList list, UnaryOperator<ItemStack> realStack) {
            if (list instanceof ItemTagList tagList) return getREIIngredients(tagList, realStack);
            if (list instanceof ItemStackList stackList) return getREIIngredients(stackList, realStack);
            return Collections.emptyList();
        }
    }

    public static final class EMICallWrapper {

        private static EmiIngredient toEMIIngredient(Stream<ItemStack> stream, UnaryOperator<ItemStack> realStack) {
            return EmiIngredient.of(stream.map(realStack).map(EmiStack::of).toList());
        }

        public static List<Object> getEMIIngredients(ItemStackList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            return List.of(toEMIIngredient(list.stream(), realStack).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(ItemTagList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            return list.getEntries().stream()
                    .map(ItemTagList.ItemTagEntry::stacks)
                    .map(stream -> toEMIIngredient(stream, realStack).setChance(xeiChance))
                    .collect(Collectors.toList());
        }

        public static List<Object> getEMIIngredients(ItemEntryList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            if (list instanceof ItemTagList tagList) return getEMIIngredients(tagList, xeiChance, realStack);
            if (list instanceof ItemStackList stackList) return getEMIIngredients(stackList, xeiChance, realStack);
            return Collections.emptyList();
        }
    }
}
