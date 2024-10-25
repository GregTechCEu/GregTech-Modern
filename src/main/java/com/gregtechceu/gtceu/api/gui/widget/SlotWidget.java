package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.transfer.item.CycleItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.TagOrCycleItemStackHandler;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.runtime.ConfiguratorParser;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@LDLRegister(name = "item_slot", group = "widget.container", priority = 50)
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
                if (slotHandler.itemHandler instanceof CycleItemStackHandler cycleHandler) {
                    return getXEIIngredientsFromCycleHandlerClickable(cycleHandler, slotHandler.index);
                } else if (slotHandler.itemHandler instanceof TagOrCycleItemStackHandler tagHandler) {
                    return getXEIIngredientsFromTagOrCycleHandlerClickable(tagHandler, slotHandler.index);
                }
            }

            if (LDLib.isJeiLoaded() && !realStack.isEmpty()) {
                return JEIPlugin.getItemIngredient(realStack, getPositionX(), getPositionY(), getSizeWidth(),
                        getSizeHeight());
            } else if (LDLib.isReiLoaded()) {
                return REICallWrapper.getReiIngredients(realStack);
            } else if (LDLib.isEmiLoaded()) {
                return EMICallWrapper.getEmiIngredients(realStack, getXEIChance());
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
            if (slotHandler.itemHandler instanceof CycleItemStackHandler cycleHandler) {
                return getXEIIngredientsFromCycleHandlerClickable(cycleHandler, slotHandler.index);
            } else if (slotHandler.itemHandler instanceof TagOrCycleItemStackHandler tagHandler) {
                return getXEIIngredientsFromTagOrCycleHandlerClickable(tagHandler, slotHandler.index);
            }
        }

        if (LDLib.isJeiLoaded()) {
            var ingredient = JEIPlugin.getItemIngredient(realStack, getPosition().x, getPosition().y, getSize().width,
                    getSize().height);
            return ingredient == null ? Collections.emptyList() : List.of(ingredient);
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(realStack);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(realStack, getXEIChance());
        }
        return List.of(realStack);
    }

    private List<Object> getXEIIngredientsFromCycleHandler(CycleItemStackHandler handler, int index) {
        var stream = handler.getStackList(index).stream().map(this::getRealStack);
        if (LDLib.isJeiLoaded()) {
            return stream.filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromCycleHandlerClickable(CycleItemStackHandler handler, int index) {
        var stream = handler.getStackList(index).stream().map(this::getRealStack);
        if (LDLib.isJeiLoaded()) {
            return stream
                    .filter(stack -> !stack.isEmpty())
                    .map(item -> JEIPlugin.getItemIngredient(item, getPositionX(), getPositionY(), getSizeWidth(),
                            getSizeHeight()))
                    .toList();
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromTagOrCycleHandler(TagOrCycleItemStackHandler handler, int index) {
        Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> either = handler
                .getStacks()
                .get(index);
        var ref = new Object() {

            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.ITEM
                                .getTag(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(item -> getRealStack(new ItemStack(item.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(this::getRealStack, list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(items -> {
            var stream = items.stream().map(this::getRealStack);
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream
                        .filter(stack -> !stack.isEmpty())
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
    }

    private List<Object> getXEIIngredientsFromTagOrCycleHandlerClickable(TagOrCycleItemStackHandler handler,
                                                                         int index) {
        Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> either = handler
                .getStacks()
                .get(index);
        var ref = new Object() {

            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.ITEM
                                .getTag(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(item -> JEIPlugin.getItemIngredient(
                                        getRealStack(new ItemStack(item.value(), pair.getSecond())),
                                        getPosition().x, getPosition().y, getSize().width, getSize().height)))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(this::getRealStack, list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(items -> {
            var stream = items.stream().map(this::getRealStack);
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream
                        .filter(stack -> !stack.isEmpty())
                        .map(item -> JEIPlugin.getItemIngredient(item, getPosition().x, getPosition().y,
                                getSize().width, getSize().height))
                        .toList();
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
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
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) &&
                    (!stack.isEmpty() && this.itemHandler.isItemValid(this.index, stack));
        }

        @Override
        public boolean mayPickup(@Nullable Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && !this.itemHandler.extractItem(index, 1, true).isEmpty();
        }

        @Override
        @Nonnull
        public ItemStack getItem() {
            return this.itemHandler.getStackInSlot(index);
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
            this.setChanged();
        }

        @Override
        public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {}

        @Override
        public int getMaxStackSize() {
            return this.itemHandler.getSlotLimit(this.index);
        }

        @Override
        public int getMaxStackSize(@Nonnull ItemStack stack) {
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
}
