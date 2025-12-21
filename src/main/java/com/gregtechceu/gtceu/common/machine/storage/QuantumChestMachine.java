package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuantumChestMachine extends TieredMachine implements IAutoOutputItem, IInteractedMachine, IControllable,
                                 IDropSaveMachine, IMuiMachine {

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
    @Persisted
    private boolean isVoiding;

    private final long maxAmount;
    protected final ItemCache cache;
    @DescSynced
    private final CustomItemStackHandler lockedItem;

    @Getter
    @DescSynced
    protected ItemStack stored = ItemStack.EMPTY;
    @Getter
    @DescSynced
    protected long storedAmount = 0;

    @Nullable
    protected TickableSubscription autoOutputSubs;

    public QuantumChestMachine(IMachineBlockEntity holder, int tier, long maxAmount, Object... args) {
        super(holder, tier);
        this.outputFacingItems = getFrontFacing().getOpposite();
        this.maxAmount = maxAmount;
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

    protected ItemCache createCacheItemHandler(Object... args) {
        return new ItemCache(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
    }

    protected void onItemChanged() {
        if (!isRemote()) {
            updateAutoOutputSubscription();
        }
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public boolean saveBreak() {
        return !stored.isEmpty();
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (!forDrop) tag.put("lockedItem", lockedItem.serializeNBT());
        tag.put("stored", stored.serializeNBT());
        tag.putLong("storedAmount", storedAmount);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        lockedItem.deserializeNBT(tag.getCompound("lockedItem"));
        stored = ItemStack.of(tag.getCompound("stored"));
        storedAmount = tag.getLong("storedAmount");
    }

    //////////////////////////////////////
    // ****** Capability ********//
    //////////////////////////////////////

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == getFrontFacing()) {
            return null;
        }
        return super.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == getFrontFacing()) {
            return null;
        }
        return super.getFluidHandlerCap(side, useCoverCapability);
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
        if ((isAutoOutputItems() && !stored.isEmpty()) && outputFacing != null &&
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
            // Check to see if the hit is within the glass frame of the chest
            var aabb = new AABB(hit.getBlockPos()).deflate(0.12);
            var hitVector = hit.getLocation().relative(getFrontFacing(), -0.5);
            if (!aabb.contains(hitVector)) return InteractionResult.PASS;

            var held = player.getMainHandItem();
            if (!held.isEmpty() && cache.canInsert(held)) { // push
                var remaining = cache.insertItem(0, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                return InteractionResult.SUCCESS;
            } else if (isDoubleHit(player.getUUID())) {
                for (var stack : player.getInventory().items) {
                    if (!stack.isEmpty() && cache.canInsert(stack)) {
                        stack.setCount(cache.insertItem(0, stack, false).getCount());
                    }
                }
            }
            INTERACTION_LOGGER.put(player.getUUID(), System.currentTimeMillis());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean isDoubleHit(UUID uuid) {
        return (System.currentTimeMillis() - INTERACTION_LOGGER.getLong(uuid)) < 300;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (player.getItemInHand(hand).is(GTToolType.WRENCH.itemTags.get(0))) return false;
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isShiftKeyDown() ? stored.getMaxStackSize() : 1, false);
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
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
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
            var copied = stored.copyWithCount(1);
            lockedItem.setStackInSlot(0, copied);
        } else if (!locked) {
            lockedItem.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public ItemStack getLockedItem() {
        return lockedItem.getStackInSlot(0);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        LongSyncValue itemSyncer = new LongSyncValue(this::getStoredAmount, (ignored) -> {});
        syncManager.syncValue("item_amount", itemSyncer);
        // SlotGroup group = new SlotGroup("item_inv", 1, 0, true);

        return new ModularPanel(this.getDefinition().getName())
                .child(
                        // Top half of the screen
                        new ParentWidget<>()
                                .widthRel(1)
                                .height(20 + 60)
                                // Box that has the display texture BG +
                                // the buttons / text / etc
                                .child(new ParentWidget<>()
                                        .background(GTGuiTextures.DISPLAY)
                                        .size(90, 63)
                                        .align(Alignment.CENTER)
                                        .child(IKey.lang("gtceu.machine.quantum_chest.items_stored").asWidget()
                                                .color(0xffffff)
                                                .margin(8, 0, 8, 0))
                                        .child(IKey.dynamic(
                                                () -> Component.literal(
                                                        FormattingUtil.formatNumbers(itemSyncer.getLongValue())))
                                                .asWidget()
                                                .color(0xffffff)
                                                .margin(8, 0, 18, 0))
                                        .child(Flow.row()
                                                .margin(4, 0, 41, 0)
                                                .coverChildren()
                                                .child(createAutoOutputItemButton(syncManager))
                                                .child(createItemLockButton(syncManager))
                                                .child(createVoidButton(syncManager)))
                                        .child(Flow.column()
                                                .margin(68, 0, 23, 0)
                                                .coverChildren()
                                                // .child(GTMuiMachineUtil.createSquareSlotGroupFromInventory(
                                                // cache, "stored",
                                                // syncManager))
                                                /*
                                                 * .child(GTMuiMachineUtil.createSlotGroupFromInventory(
                                                 * cache, "stored",
                                                 * 1, 'B',
                                                 * slot -> slot.background(GTGuiTextures.SLOT, GTGuiTextures.SLOT),
                                                 * syncManager,
                                                 * "B"))
                                                 */
                                                .child(createImportItemSlot(syncManager))
                                                .child(createOutputItemSlot(syncManager))
                                        // .child(createPhantomLockeditemSlot(syncManager))
                                        ))

                )
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    private ToggleButton createAutoOutputItemButton(PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(this::isAutoOutputItems,
                this::setAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(
                        (r) -> r.addLine(IKey.lang(Component.translatable("gtceu.machine.quantum_chest.items_stored",
                                Component.translatable(itemOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                        "cover.voiding.label.disabled")))));
    }

    private ToggleButton createItemLockButton(PanelSyncManager syncManager) {
        BooleanSyncValue itemLocked = new BooleanSyncValue(this::isLocked,
                this::setLocked);
        syncManager.syncValue("item_locked", itemLocked);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemLocked::getBoolValue, itemLocked::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_LOCK)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(itemLocked.getBoolValue() ?
                        "gtceu.gui.item_lock.tooltip.enabled" :
                        "gtceu.gui.item_lock.tooltip.disabled")));
    }

    private ToggleButton createVoidButton(PanelSyncManager syncManager) {
        BooleanSyncValue voiding = new BooleanSyncValue(() -> this.isVoiding,
                (voidingBool) -> { this.isVoiding = voidingBool; });
        syncManager.syncValue("is_voiding", voiding);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(voiding::getBoolValue, voiding::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_VOID)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(voiding.getBoolValue() ?
                        "gtceu.gui.item_voiding_partial.tooltip.enabled" :
                        "gtceu.gui.item_voiding_partial.tooltip.disabled")));
    }

    private IWidget createImportItemSlot(PanelSyncManager syncManager) {
        var importItems = createImportItems();
        ItemSlot slot = new ItemSlot()
                .slot(SyncHandlers.itemSlot(importItems, 0)
                        // .slotGroup(group)
                        .changeListener((newItem, amount, client, init) -> {
                            if (amount) {
                                importItems.onContentsChanged(0);
                            }
                        })
                        // .ignoreMaxStackSize(true)
                        .accessibility(true, false));
        // slot.getSlot().isPhantom()
        // syncManager.syncValue("item_slot",SyncHandlers.itemSlot(slot));
        return slot;
    }

    private IWidget createOutputItemSlot(PanelSyncManager syncManager) {
        ItemSlot slot = new ItemSlot()
                .slot(SyncHandlers.itemSlot(cache, 0)
                        // .slotGroup(group)
                        .changeListener((newItem, amount, client, init) -> {
                            if (amount) {
                                cache.onChanged();
                            }
                        }).ignoreMaxStackSize(true)
                        .accessibility(false, true));
        // slot.getSlot().isPhantom()
        // syncManager.syncValue("item_slot",SyncHandlers.itemSlot(slot));
        return slot;
    }
    /*
     * private IWidget createPhantomLockedFluidSlot(PanelSyncManager syncManager) {
     * syncManager.syncValue("locked_fluid_slot",
     * new ItemSlot(lockedItem).controlsAmount(false).phantom(true));
     * return new FluidSlot().syncHandler("locked_fluid_slot", 0).background(GTGuiTextures.FLUID_SLOT);
     * }
     */

    /*
     * public Widget createUIWidget() {
     * var group = new WidgetGroup(0, 0, 109, 63);
     * var importItems = createImportItems();
     * group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))
     * .addWidget(new LabelWidget(8, 8, "gtceu.machine.quantum_chest.items_stored"))
     * .addWidget(new LabelWidget(8, 18, () -> FormattingUtil.formatNumbers(storedAmount))
     * .setTextColor(-1)
     * .setDropShadow(true))
     * .addWidget(new SlotWidget(importItems, 0, 87, 5, false, true)
     * .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY)))
     * .addWidget(new SlotWidget(cache, 0, 87, 23, false, false)
     * .setItemHook(s -> s.copyWithCount((int) Math.min(storedAmount, s.getMaxStackSize())))
     * .setBackgroundTexture(GuiTextures.SLOT))
     * .addWidget(new ButtonWidget(87, 42, 18, 18,
     * new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, Icons.DOWN.scale(0.7f)), cd -> {
     * if (!cd.isRemote) {
     * if (!stored.isEmpty()) {
     * var extracted = cache.extractItem(0,
     * (int) Math.min(storedAmount, stored.getMaxStackSize()), false);
     * if (!group.getGui().entityPlayer.addItem(extracted)) {
     * Block.popResource(group.getGui().entityPlayer.level(),
     * group.getGui().entityPlayer.getOnPos(), extracted);
     * }
     * }
     * }
     * }))
     * .addWidget(new PhantomSlotWidget(lockedItem, 0, 58, 41,
     * stack -> stored.isEmpty() || ItemStack.isSameItemSameTags(stack, stored))
     * .setMaxStackSize(1))
     * .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
     * GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
     * .setShouldUseBaseBackground()
     * .setTooltipText("gtceu.gui.item_auto_output.tooltip"))
     * .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
     * GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
     * .setShouldUseBaseBackground()
     * .setTooltipText("gtceu.gui.item_lock.tooltip"))
     * .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
     * GuiTextures.BUTTON_VOID, () -> isVoiding, (b) -> isVoiding = b)
     * .setShouldUseBaseBackground()
     * .setTooltipText("gtceu.gui.item_voiding_partial.tooltip"));
     * group.setBackground(GuiTextures.BACKGROUND_INVERSE);
     * return group;
     * }
     */
    private @NotNull CustomItemStackHandler createImportItems() {
        var importItems = new CustomItemStackHandler();
        importItems.setFilter(cache::canInsert);
        importItems.setOnContentsChanged(() -> {
            var item = importItems.getStackInSlot(0).copy();
            if (!item.isEmpty()) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
                importItems.onContentsChanged(0);
                cache.insertItem(0, item.copy(), false);
            }
        });
        return importItems;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
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

    protected class ItemCache extends MachineTrait implements IItemHandlerModifiable {

        private final Predicate<ItemStack> filter = i -> !isLocked() ||
                ItemStack.isSameItemSameTags(i, getLockedItem());

        public ItemCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            stored = stack.copyWithCount(1);
            storedAmount = stack.getCount();
            onItemChanged();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return stored.copyWithCount(GTMath.saturatedCast(storedAmount));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            long free = isVoiding ? Long.MAX_VALUE : maxAmount - storedAmount;
            long canStore = 0;
            if ((stored.isEmpty() || ItemHandlerHelper.canItemStacksStack(stored, stack)) && filter.test(stack)) {
                canStore = Math.min(stack.getCount(), free);
            }
            if (!simulate && canStore > 0) {
                if (stored.isEmpty()) stored = stack.copyWithCount(1);
                storedAmount = Math.min(maxAmount, storedAmount + canStore);
                onItemChanged();
            }
            return stack.copyWithCount((int) (stack.getCount() - canStore));
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (stored.isEmpty()) return ItemStack.EMPTY;
            long toExtract = Math.min(storedAmount, amount);
            var copy = stored.copyWithCount((int) toExtract);
            if (!simulate && toExtract > 0) {
                storedAmount -= toExtract;
                if (storedAmount == 0) stored = ItemStack.EMPTY;
                onItemChanged();
            }
            return copy;
        }

        @Override
        public int getSlotLimit(int slot) {
            return GTMath.saturatedCast(maxAmount);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return filter.test(stack);
        }

        public void exportToNearby(@NotNull Direction... facings) {
            if (stored.isEmpty()) return;
            var level = getMachine().getLevel();
            var pos = getMachine().getPos();
            for (Direction facing : facings) {
                var filter = getMachine().getItemCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                        .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
            }
        }

        public boolean canInsert(ItemStack stack) {
            return filter.test(stack) && (insertItem(0, stack, true).getCount() != stack.getCount());
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
