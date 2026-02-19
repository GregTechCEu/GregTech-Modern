package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
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
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PhantomItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@NotNullByDefault
public class QuantumChestMachine extends TieredMachine implements IControllable,
                                 IDropSaveMachine, IMuiMachine {

    /**
     * Sourced from FunctionalStorage's
     * <a
     * href=https://github.com/Buuz135/FunctionalStorage/blob/1.21/src/main/java/com/buuz135/functionalstorage/block/tile/ItemControllableDrawerTile.java>
     * ItemControllerDrawerTile</a>
     */
    public static final Object2LongOpenHashMap<UUID> INTERACTION_LOGGER = new Object2LongOpenHashMap<>();

    @SaveField
    private boolean isVoiding;

    private final long maxAmount;
    protected final ItemCache cache;
    @SyncToClient
    @SaveField
    private final CustomItemStackHandler lockedItem;

    @Getter
    @SyncToClient
    @SaveField
    protected ItemStack stored = ItemStack.EMPTY;
    @Getter
    @SyncToClient
    @SaveField
    protected long storedAmount = 0;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public QuantumChestMachine(BlockEntityCreationInfo info, int tier, long maxAmount) {
        super(info, tier);
        this.maxAmount = maxAmount;
        this.cache = createCacheItemHandler();
        this.lockedItem = new CustomItemStackHandler();
        this.autoOutput = AutoOutputTrait.ofItems(this, cache);
        lockedItem.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("lockedItem"));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected ItemCache createCacheItemHandler() {
        return new ItemCache(this);
    }

    protected void onItemChanged() {
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("storedAmount");
            syncDataHolder.markClientSyncFieldDirty("stored");
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
    public void saveToItem(CompoundTag tag) {
        tag.put("stored", stored.save(new CompoundTag()));
        tag.putLong("storedAmount", storedAmount);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
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
    public boolean isWorkingEnabled() {
        return autoOutput.isAutoOutputItems();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        autoOutput.setAllowAutoOutputItems(isWorkingAllowed);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

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
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private static boolean isDoubleHit(UUID uuid) {
        return (System.currentTimeMillis() - INTERACTION_LOGGER.getLong(uuid)) < 300;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos,
                               @Nullable Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (GTToolType.WRENCH.matchTags.stream().anyMatch(player.getItemInHand(hand)::is)) return false;
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isShiftKeyDown() ? stored.getMaxStackSize() : 1, false);
                if (!drained.isEmpty()) {
                    if (!player.addItem(drained)) {
                        Block.popResourceFromFace(world, getBlockPos(), getFrontFacing(), drained);
                    }
                }
            }
        }
        return super.onLeftClick(player, world, hand, pos, direction);
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
                                                .margin(68, 0, 15, 0)
                                                .coverChildren()
                                                .child(createItemSlot(syncManager))
                                                .child(createPhantomLockeditemSlot(syncManager))))

                )
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    private ToggleButton createAutoOutputItemButton(PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(this.autoOutput::isAutoOutputItems,
                this.autoOutput::setAllowAutoOutputItems);
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

    private IWidget createItemSlot(PanelSyncManager syncManager) {
        ItemSlotSyncHandler slot = new ItemSlotSyncHandler(new ModularSlot(cache, 0)
                .ignoreMaxStackSize(true)
                .slotGroup(new SlotGroup("stored", 1, true)));
        syncManager.syncValue("stored", 1, slot);

        return new ItemSlot().syncHandler("stored", 1);
    }

    private IWidget createPhantomLockeditemSlot(PanelSyncManager syncManager) {
        lockedItem.setOnContentsChanged(() -> lockedItem.getStackInSlot(0).setCount(1));
        PhantomItemSlotSyncHandler lockSlot = new PhantomItemSlotSyncHandler(new ModularSlot(lockedItem, 0).filter(
                stack -> stored.isEmpty() || ItemStack.isSameItemSameTags(stack, stored)));

        syncManager.syncValue("lock", lockSlot);
        return new PhantomItemSlot().syncHandler("lock");
    }

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
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (side == getFrontFacing()) return null;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    protected class ItemCache extends MachineTrait implements IItemHandlerModifiable {

        public static final MachineTraitType<ItemCache> TYPE = new MachineTraitType<>(ItemCache.class);

        @Override
        public MachineTraitType<ItemCache> getTraitType() {
            return TYPE;
        }

        private final Predicate<ItemStack> filter = i -> !isLocked() ||
                GTUtil.isSameItemSameTags(i, getLockedItem());

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
            var pos = getMachine().getBlockPos();
            for (Direction facing : facings) {
                var filter = getMachine().getItemCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                        .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
            }
        }

        public boolean canInsert(ItemStack stack) {
            return filter.test(stack) && (insertItem(0, stack, true).getCount() != stack.getCount());
        }
    }
}
