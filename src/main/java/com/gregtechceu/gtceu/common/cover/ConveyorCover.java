package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.*;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.ItemHandlerDelegate;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorCover extends CoverBehavior implements IIOCover, IMuiCover, IControllable {

    // 8 32 128 512 1024
    public static final Int2IntFunction CONVEYOR_SCALING = tier -> 2 * (int) Math.pow(4, Math.min(tier, GTValues.LuV));

    public final int tier;
    public final int maxItemTransferRate;
    @SaveField
    @Getter
    protected int transferRate;
    @SaveField
    @SyncToClient
    @Getter
    @RerenderOnChanged
    protected IO io;
    @SaveField
    @SyncToClient
    @Getter
    protected DistributionMode distributionMode;
    @SaveField
    @SyncToClient
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @SaveField
    @SyncToClient
    @Getter
    protected boolean isWorkingEnabled = true;
    protected int itemsLeftToTransferLastSecond;

    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier,
                         int maxTransferRate) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = maxTransferRate;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.io = IO.OUT;
        this.distributionMode = DistributionMode.INSERT_FIRST;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.item(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(this::configureFilter);
    }

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        this(definition, coverHolder, attachedSide, tier, CONVEYOR_SCALING.applyAsInt(tier));
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentItemHandler() != null;
    }

    protected @Nullable IItemHandlerModifiable getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IItemHandler getAdjacentItemHandler() {
        return GTTransferUtils.getAdjacentItemHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .resolve().orElse(null);
    }

    public void setDistributionMode(DistributionMode mode) {
        distributionMode = mode;
        syncDataHolder.markClientSyncFieldDirty("distributionMode");
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public boolean canAttach() {
        return super.canAttach() && getOwnItemHandler() != null;
    }

    public void setTransferRate(int transferRate) {
        if (transferRate <= maxItemTransferRate) {
            this.transferRate = transferRate;
        }
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
        }
        subscriptionHandler.updateSubscription();
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    //////////////////////////////////////
    // ***** Transfer Logic *****//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        subscriptionHandler.updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (itemsLeftToTransferLastSecond > 0) {
                var adjacent = getAdjacentItemHandler();
                var self = getOwnItemHandler();

                if (adjacent != null && self != null) {
                    int totalTransferred = switch (io) {
                        case IN -> doTransferItems(adjacent, self, itemsLeftToTransferLastSecond);
                        case OUT -> doTransferItems(self, adjacent, itemsLeftToTransferLastSecond);
                        default -> 0;
                    };
                    this.itemsLeftToTransferLastSecond -= totalTransferred;
                }
            }
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
            subscriptionHandler.updateSubscription();
        }
    }

    protected int doTransferItems(IItemHandler sourceInventory, IItemHandler targetInventory, int maxTransferAmount) {
        return moveInventoryItems(sourceInventory, targetInventory, maxTransferAmount);
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     int maxTransferAmount) {
        ItemFilter filter = filterHandler.getFilter();
        int itemsLeftToTransfer = maxTransferAmount;

        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            if (!filter.test(sourceStack)) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    ItemHandlerHelper.insertItem(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected static boolean moveInventoryItemsExact(IItemHandler sourceInventory, IItemHandler targetInventory,
                                                     TypeItemInfo itemInfo) {
        // first, compute how much can we extract in reality from the machine,
        // because totalCount is based on what getStackInSlot returns, which may differ from what
        // extractItem() will return
        ItemStack resultStack = itemInfo.itemStack.copy();
        int totalExtractedCount = 0;
        int itemsLeftToExtract = itemInfo.totalCount;

        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.getInt(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, true);
            if (!extractedStack.isEmpty() &&
                    GTUtil.isSameItemSameTags(resultStack, extractedStack)) {
                totalExtractedCount += extractedStack.getCount();
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        // if amount of items extracted is not equal to the amount of items we
        // wanted to extract, abort item extraction
        if (totalExtractedCount != itemInfo.totalCount) {
            return false;
        }
        // adjust size of the result stack accordingly
        resultStack.setCount(totalExtractedCount);

        // now, see how much we can insert into destination inventory
        // if we can't insert as much as itemInfo requires, and remainder is empty, abort, abort
        ItemStack remainder = ItemHandlerHelper.insertItem(targetInventory, resultStack, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        // otherwise, perform real insertion and then remove items from the source inventory
        ItemHandlerHelper.insertItem(targetInventory, resultStack, false);

        // perform real extraction of the items from the source inventory now
        itemsLeftToExtract = itemInfo.totalCount;
        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.getInt(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, false);
            if (!extractedStack.isEmpty() &&
                    GTUtil.isSameItemSameTags(resultStack, extractedStack)) {
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        return true;
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     Map<ItemStack, GroupItemInfo> itemInfos, int maxTransferAmount) {
        ItemFilter filter = filterHandler.getFilter();
        int itemsLeftToTransfer = maxTransferAmount;

        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty() || !filter.test(itemStack) || !itemInfos.containsKey(itemStack)) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(itemStack);

            ItemStack extractedStack = sourceInventory.extractItem(i,
                    Math.min(itemInfo.totalCount, itemsLeftToTransfer), true);

            ItemStack remainderStack = ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, true);
            int amountToInsert = extractedStack.getCount() - remainderStack.getCount();

            if (amountToInsert > 0) {
                extractedStack = sourceInventory.extractItem(i, amountToInsert, false);

                if (!extractedStack.isEmpty()) {

                    ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, false);
                    itemsLeftToTransfer -= extractedStack.getCount();
                    itemInfo.totalCount -= extractedStack.getCount();

                    if (itemInfo.totalCount == 0) {
                        itemInfos.remove(itemStack);
                        if (itemInfos.isEmpty()) {
                            break;
                        }
                    }
                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(IItemHandler inventory) {
        ItemFilter filter = filterHandler.getFilter();
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !filter.test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new TypeItemInfo(s, new IntArrayList(), 0));

            itemInfo.totalCount += itemStack.getCount();
            itemInfo.slots.add(srcIndex);
        }

        return result;
    }

    protected Map<ItemStack, GroupItemInfo> countInventoryItemsByMatchSlot(IItemHandler inventory) {
        ItemFilter filter = filterHandler.getFilter();
        Map<ItemStack, GroupItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !filter.test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new GroupItemInfo(s, 0));

            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }

    @AllArgsConstructor
    protected static class TypeItemInfo {

        public final ItemStack itemStack;
        public final IntList slots;
        public int totalCount;
    }

    @AllArgsConstructor
    protected static class GroupItemInfo {

        public final ItemStack itemStack;
        public int totalCount;
    }

    public boolean shouldRespectDistributionMode() {
        return ((io == IO.IN) ?
                (coverHolder.getLevel().getBlockEntity(coverHolder.getBlockPos()) instanceof ItemPipeBlockEntity) :
                (coverHolder.getLevel().getBlockEntity(coverHolder.getBlockPos()
                        .relative(attachedSide)) instanceof ItemPipeBlockEntity));
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        EnumSyncValue<ManualIOMode> manualMode = new EnumSyncValue<>(ManualIOMode.class,
                this::getManualIOMode, this::setManualIOMode);

        EnumSyncValue<DistributionMode> distMode = new EnumSyncValue<>(DistributionMode.class,
                this::getDistributionMode, this::setDistributionMode);

        IntSyncValue transferRate = new IntSyncValue(this::getTransferRate, this::setTransferRate);
        EnumSyncValue<IO> ioSync = new EnumSyncValue<>(IO.class, this::getIo, this::setIo);

        syncManager.syncValue("io", ioSync);
        syncManager.syncValue("manualMode", manualMode);
        syncManager.syncValue("distribution", distMode);
        syncManager.syncValue("throughput", transferRate);

        if (createThroughputRow()) {
            column.child(GTMuiWidgets.createIntInputWithButtons(transferRate, () -> 1, () -> maxItemTransferRate));
        }

        if (createFilterRow()) {
            column.child(GTMuiWidgets.createFilterRow(filterHandler, data, syncManager, settings)
                    .child(0, GTMuiWidgets.createIOCycleButton(ioSync, false)));
        }

        if (createDistributionModeRow()) {
            column.child(new GTMuiWidgets.EnumRowBuilder<>(DistributionMode.class)
                    .value(distMode)
                    .overlay(16, GTGuiTextures.DISTRIBUTION_MODE_OVERLAY)
                    .lang(Text.dynamic(() -> Component.translatable(distributionMode.localeName)))
                    .build());
        }

        if (createManualIOModeRow()) {
            column.child(new GTMuiWidgets.EnumRowBuilder<>(ManualIOMode.class)
                    .value(manualMode)
                    .overlay(16, GTGuiTextures.MANUAL_IO_OVERLAY_IN)
                    .lang(Text.dynamic(() -> Component.translatable(manualIOMode.localeName)))
                    .build());

        }
    }

    protected boolean createThroughputRow() {
        return true;
    }

    protected boolean createFilterRow() {
        return true;
    }

    protected boolean createConveyorIORow() {
        return true;
    }

    protected boolean createDistributionModeRow() {
        return true;
    }

    protected boolean createManualIOModeRow() {
        return true;
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /////////////////////////////////////

    private @Nullable CoverableItemHandlerWrapper itemHandlerWrapper;

    @Nullable
    @Override
    public IItemHandlerModifiable getItemHandlerCap(@Nullable IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != defaultValue) {
            this.itemHandlerWrapper = new CoverableItemHandlerWrapper(defaultValue);
        }
        return itemHandlerWrapper;
    }

    private class CoverableItemHandlerWrapper extends ItemHandlerDelegate {

        public CoverableItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (io == IO.OUT) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!filterHandler.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (io == IO.IN) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !filterHandler.test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("transferRate", getTransferRate());
        tag.putInt("io", getIo().ordinal());
        tag.putInt("distributionMode", getDistributionMode().ordinal());
        tag.putInt("manualIO", getManualIOMode().ordinal());
        tag.put("filter", filterHandler.getFilterItem().serializeNBT());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setTransferRate(tag.getInt("transferRate"));
        setIo(IO.values()[tag.getInt("io")]);
        setDistributionMode(DistributionMode.values()[tag.getInt("distributionMode")]);
        setManualIOMode(ManualIOMode.values()[tag.getInt("manualIO")]);
        filterHandler.setFilterItem(ItemStack.of(tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }
}
