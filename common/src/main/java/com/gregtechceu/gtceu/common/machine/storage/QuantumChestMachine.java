package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuantumChestMachine extends TieredMachine implements IAutoOutputItem, IInteractedMachine, IControllable, IDropSaveMachine, IFancyUIMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuantumChestMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter @Persisted @DescSynced @RequireRerender
    protected Direction outputFacingItems;
    @Getter @Persisted @DescSynced @RequireRerender
    protected boolean autoOutputItems;
    @Getter @Setter @Persisted
    protected boolean allowInputFromOutputSideItems;
    @Getter
    private final int maxStoredItems;
    @Persisted @DescSynced @DropSaved
    protected int itemsStoredInside = 0;
    @Getter @Persisted @DescSynced @DropSaved
    private int storedAmount = 0;
    @Getter @Persisted @DescSynced @DropSaved @Nonnull
    private ItemStack stored = ItemStack.EMPTY;
    @Persisted @DropSaved
    protected final NotifiableItemStackHandler cache;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportItemSubs;
    @Persisted @Getter @Setter
    private boolean isVoiding;
    @Persisted @Getter
    private final ItemStackTransfer lockedItem;

    public QuantumChestMachine(IMachineBlockEntity holder, int tier, int maxStoredItems, Object... args) {
        super(holder, tier);
        this.outputFacingItems = getFrontFacing().getOpposite();
        this.maxStoredItems = maxStoredItems;
        this.cache = createCacheItemHandler(args);
        this.lockedItem = new ItemStackTransfer();
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.BOTH, IO.BOTH) {

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                var item = super.getStackInSlot(slot).copy();
                if (!item.isEmpty()) {
                    item.setCount(Math.min(itemsStoredInside + item.getCount(), Integer.MAX_VALUE));
                }
                return item;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? maxStoredItems : 0;
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                var remained = super.insertItem(slot, stack, simulate).copy();
                if (!remained.isEmpty()) {
                    if (ItemTransferHelper.canItemStacksStack(getStackInSlot(0), remained)) {
                        int added = Math.min(maxStoredItems - itemsStoredInside, remained.getCount());
                        if (!simulate) {
                            itemsStoredInside += added;
                            onContentsChanged();
                        }
                        remained.shrink(added);
                        if (isVoiding) {
                            remained.setCount(0);
                        }
                    }
                }
                return remained;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                var extracted = super.extractItem(slot, amount, simulate).copy();
                if (!extracted.isEmpty()) {
                    var additional = Math.min(amount - extracted.getCount(), itemsStoredInside);
                    extracted.grow(additional);
                    if (!simulate) {
                        itemsStoredInside -= additional;
                        if (getStackInSlot(0).isEmpty() && itemsStoredInside > 0) {
                            var copied = extracted.copy();
                            copied.setCount(Math.min(itemsStoredInside, Math.min(64, copied.getItem().getMaxStackSize())));
                            itemsStoredInside -= copied.getCount();
                            setStackInSlot(0, copied);
                        }
                        onContentsChanged();
                    }
                }
                return extracted;
            }

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                if (!isRemote()) {
                    stored = getStackInSlot(0).copy();
                    storedAmount = stored.getCount();
                    stored.setCount(1);
                }
            }

        }.setFilter(itemStack -> !isLocked() || ItemTransferHelper.canItemStacksStack(lockedItem.getStackInSlot(0), itemStack));

    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
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
    //*******     Auto Output    *******//
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
        if ((isAutoOutputItems() && !cache.isEmpty()) && outputFacing != null
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(outputFacing), outputFacing.getOpposite()) != null) {
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
    //*******     Interaction    *******//
    //////////////////////////////////////

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == outputFacingItems) return false;
        return super.isFacingValid(facing);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() == getFrontFacing()) {
            var held = player.getMainHandItem();
            if (!held.isEmpty() && (ItemTransferHelper.canItemStacksStack(held, stored) || stored.isEmpty())) { // push
                if (!isRemote()) {
                    var remaining = cache.insertItem(0, held, false);
                    player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isCrouching() ? stored.getItem().getMaxStackSize() : 1, false);
                if (!drained.isEmpty()) {
                    if (player.addItem(drained)) {
                        Block.popResource(world, getPos().relative(getFrontFacing()), drained);
                    }
                }
            }
        }
        return IInteractedMachine.super.onLeftClick(player, world, hand, pos, direction);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!playerIn.isCrouching() && !isRemote()) {
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
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (gridSide == getOutputFacingItems()) {
                if (isAllowInputFromOutputSideItems()) {
                    setAllowInputFromOutputSideItems(false);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.disallow").append(Component.translatable("gtceu.creative.chest.item")));
                } else {
                    setAllowInputFromOutputSideItems(true);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.allow").append(Component.translatable("gtceu.creative.chest.item")));
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
    //***********     GUI    ***********//
    //////////////////////////////////////

    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 109, 63);
        var importItems = new ItemStackTransfer();
        importItems.setFilter(itemStack -> {
            var item = cache.getStackInSlot(0);
            return (maxStoredItems - storedAmount) > itemStack.getCount() &&
                    ((item.isEmpty() && (!isLocked() || ItemTransferHelper.canItemStacksStack(itemStack, getLockedItem().getStackInSlot(0))))
                            || ItemTransferHelper.canItemStacksStack(itemStack, item));
        });
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
                        .setItemHook(itemStack -> {
                            var copied = itemStack.copy();
                            if (!copied.isEmpty()) {
                                copied.setCount(1);
                            }
                            return copied;
                        })
                        .setBackgroundTexture(GuiTextures.SLOT))
                .addWidget(new ButtonWidget(87, 42, 18, 18,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, Icons.DOWN.scale(0.7f)),cd -> {
                    if (!cd.isRemote) {
                        var stored = cache.getStackInSlot(0);
                        if (!stored.isEmpty()) {
                            var extracted = cache.extractItem(0, Math.min(stored.getCount(), stored.getItem().getMaxStackSize()), false);
                            if (!group.getGui().entityPlayer.addItem(extracted)) {
                                Block.popResource(group.getGui().entityPlayer.level(), group.getGui().entityPlayer.getOnPos(), extracted);
                            }
                        }
                    }
                }))
                .addWidget(new PhantomSlotWidget(lockedItem, 0, 58, 41))
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
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH) {
            if (!player.isCrouching()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        } else if (toolType == GTToolType.SCREWDRIVER) {
            if (side == getOutputFacingItems()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }
        return super.sideTips(player, toolType, side);
    }
}
