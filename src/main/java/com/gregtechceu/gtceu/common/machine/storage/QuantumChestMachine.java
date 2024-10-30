package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuantumChestMachine extends TieredMachine implements IAutoOutputItem, IInteractedMachine, IControllable,
                                 IDropSaveMachine, IFancyUIMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuantumChestMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    /**
     * Sourced from FunctionalStorage's
     * <a
     * href=https://github.com/Buuz135/FunctionalStorage/blob/1.21/src/main/java/com/buuz135/functionalstorage/block/tile/ItemControllableDrawerTile.java>
     * ItemControllerDrawerTile</a>
     */
    public static final Object2LongOpenHashMap<UUID> INTERACTION_LOGGER = new Object2LongOpenHashMap<>();

    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingItems;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputItems;
    @Getter
    @Setter
    @Persisted
    protected boolean allowInputFromOutputSideItems;
    @Getter
    private final int maxStoredItems;
    @Getter
    @Persisted
    @DescSynced
    @DropSaved
    protected int storedAmount = 0;
    @Getter
    @Persisted
    @DescSynced
    @DropSaved
    @NotNull
    protected ItemStack stored = ItemStack.EMPTY;
    @Persisted
    @DropSaved
    @Getter
    protected final NotifiableItemStackHandler cache;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportItemSubs;
    @Persisted
    @Getter
    @Setter
    private boolean isVoiding;
    @Persisted
    @DescSynced
    @Getter
    private final CustomItemStackHandler lockedItem;

    public QuantumChestMachine(IMachineBlockEntity holder, int tier, int maxStoredItems, Object... args) {
        super(holder, tier);
        this.outputFacingItems = getFrontFacing().getOpposite();
        this.maxStoredItems = maxStoredItems;
        this.cache = createCacheItemHandler(args);
        this.lockedItem = new CustomItemStackHandler();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new CustomCache(this).setFilter(itemStack -> !isLocked() ||
                ItemHandlerHelper.canItemStacksStack(lockedItem.getStackInSlot(0), itemStack));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.stored = cache.storage.getStackInSlot(0);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        exportItemSubs = cache.addChangedListener(this::onItemChanged);
    }

    private void onItemChanged() {
        if (!isRemote()) {
            this.stored = cache.storage.getStackInSlot(0);
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    //////////////////////////////////////
    // ******* Auto Output *******//
    //////////////////////////////////////

    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isAutoOutputItems();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setAutoOutputItems(isWorkingAllowed);
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems();
        if ((isAutoOutputItems() && !cache.isEmpty()) && outputFacing != null &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), outputFacing)) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::checkAutoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                cache.exportToNearby(getOutputFacingItems());
            }
            updateAutoOutputSubscription();
        }
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == outputFacingItems) return false;
        return super.isFacingValid(facing);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hit.getDirection() == getFrontFacing() && !isRemote()) {
            var held = player.getMainHandItem();
            if (!held.isEmpty() && (cache.insertItem(0, held, true).getCount() != held.getCount())) { // push
                var remaining = cache.insertItem(0, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                return InteractionResult.SUCCESS;
            } else if (System.currentTimeMillis() -
                    INTERACTION_LOGGER.getOrDefault(player.getUUID(), System.currentTimeMillis()) < 300) {
                        for (var stack : player.getInventory().items) {
                            if (!stack.isEmpty() && (cache.insertItem(0, stack, true).getCount() != stack.getCount())) {
                                stack.setCount(cache.insertItem(0, stack, false).getCount());
                            }
                        }
                    }
            INTERACTION_LOGGER.put(player.getUUID(), System.currentTimeMillis());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (player.getItemInHand(hand).is(GTToolType.WRENCH.itemTags.get(0))) return false;
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isShiftKeyDown() ? stored.getItem().getMaxStackSize() : 1,
                        false);
                if (!drained.isEmpty()) {
                    if (!player.addItem(drained)) {
                        Block.popResourceFromFace(world, getPos(), getFrontFacing(), drained);
                    }
                }
            }
        }
        return IInteractedMachine.super.onLeftClick(player, world, hand, pos, direction);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (!playerIn.isShiftKeyDown() && !isRemote()) {
            var tool = playerIn.getItemInHand(hand);
            if (tool.getDamageValue() >= tool.getMaxDamage()) return InteractionResult.PASS;
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;
            if (gridSide != getOutputFacingItems()) {
                setOutputFacingItems(gridSide);
            } else {
                setOutputFacingItems(null);
            }
            return InteractionResult.CONSUME;

        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            if (gridSide == getOutputFacingItems()) {
                if (isAllowInputFromOutputSideItems()) {
                    setAllowInputFromOutputSideItems(false);
                    playerIn.sendSystemMessage(
                            Component.translatable("gtceu.machine.basic.input_from_output_side.disallow")
                                    .append(Component.translatable("gtceu.creative.chest.item")));
                } else {
                    setAllowInputFromOutputSideItems(true);
                    playerIn.sendSystemMessage(
                            Component.translatable("gtceu.machine.basic.input_from_output_side.allow")
                                    .append(Component.translatable("gtceu.creative.chest.item")));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    public boolean isLocked() {
        return !lockedItem.getStackInSlot(0).isEmpty();
    }

    protected void setLocked(boolean locked) {
        if (!stored.isEmpty() && locked) {
            var copied = stored.copy();
            copied.setCount(1);
            lockedItem.setStackInSlot(0, copied);
        } else if (!locked) {
            lockedItem.setStackInSlot(0, ItemStack.EMPTY);
        }
        lockedItem.onContentsChanged(0);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 109, 63);
        var importItems = new CustomItemStackHandler();
        importItems.setFilter(itemStack -> cache.insertItem(0, itemStack, true).getCount() != itemStack.getCount());
        importItems.setOnContentsChanged(() -> {
            var item = importItems.getStackInSlot(0).copy();
            if (!item.isEmpty()) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
                importItems.onContentsChanged(0);
                cache.insertItem(0, item.copy(), false);
            }
        });
        var current = cache.getStackInSlot(0).copy();
        if (!current.isEmpty()) {
            current.setCount(Math.min(current.getCount(), current.getItem().getMaxStackSize()));
        }
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.machine.quantum_chest.items_stored"))
                .addWidget(new LabelWidget(8, 18, () -> storedAmount + "").setTextColor(-1).setDropShadow(true))
                .addWidget(new SlotWidget(importItems, 0, 87, 5, false, true)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY)))
                .addWidget(new SlotWidget(cache, 0, 87, 23, false, false)
                        .setItemHook(itemStack -> itemStack
                                .copyWithCount(Math.min(storedAmount, itemStack.getItem().getMaxStackSize(itemStack))))
                        .setBackgroundTexture(GuiTextures.SLOT))
                .addWidget(new ButtonWidget(87, 42, 18, 18,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, Icons.DOWN.scale(0.7f)), cd -> {
                            if (!cd.isRemote) {
                                var stored = cache.getStackInSlot(0);
                                if (!stored.isEmpty()) {
                                    var extracted = cache.extractItem(0,
                                            Math.min(storedAmount, stored.getItem().getMaxStackSize(stored)), false);
                                    if (!group.getGui().entityPlayer.addItem(extracted)) {
                                        Block.popResource(group.getGui().entityPlayer.level(),
                                                group.getGui().entityPlayer.getOnPos(), extracted);
                                    }
                                }
                            }
                        }))
                .addWidget(new PhantomSlotWidget(lockedItem, 0, 58, 41,
                        stack -> stored.isEmpty() || ItemStack.isSameItemSameTags(stack, stored))
                        .setMaxStackSize(1))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, this::isVoiding, this::setVoiding)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                    Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingItems()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (side == getFrontFacing()) return null;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    private class CustomCache extends NotifiableItemStackHandler {

        public CustomCache(MetaMachine holder) {
            super(holder, 1, IO.BOTH, IO.BOTH);
        }

        private ItemStack inner() {
            return storage.getStackInSlot(0);
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inner().copyWithCount(storedAmount);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int free = isVoiding ? Integer.MAX_VALUE : maxStoredItems - storedAmount;
            int canStore = 0;
            if ((inner().isEmpty() || ItemHandlerHelper.canItemStacksStack(inner(), stack)) &&
                    storage.getFilter().test(stack)) {
                canStore = Math.min(stack.getCount(), free);
            }
            if (!simulate && canStore > 0) {
                if (inner().isEmpty()) setStackInSlot(0, stack.copyWithCount(1));
                storedAmount = Math.min(maxStoredItems, storedAmount + canStore);
                storage.onContentsChanged(0);
            }
            if (canStore == stack.getCount()) return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - canStore);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            var stored = inner().copy();
            if (stored.isEmpty()) return ItemStack.EMPTY;
            int toExtract = Math.min(storedAmount, amount);
            if (!simulate && toExtract > 0) {
                storedAmount -= toExtract;
                if (storedAmount == 0) setStackInSlot(0, ItemStack.EMPTY);
                storage.onContentsChanged(0);
            }
            if (toExtract == 0) return ItemStack.EMPTY;
            return stored.copyWithCount(toExtract);
        }
    }
}
