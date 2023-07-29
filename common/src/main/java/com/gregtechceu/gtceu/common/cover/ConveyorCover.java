package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote ConveyorCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorCover extends CoverBehavior implements IUICover, IControllable {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConveyorCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);
    public final int tier;
    public final int maxItemTransferRate;
    @Persisted @Getter
    protected int transferRate;
    @Persisted @DescSynced @Getter @RequireRerender
    protected IO io;
    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    @Persisted @DescSynced @Getter
    protected ItemStack filterItem;
    @Nullable
    protected ItemFilter filterHandler;
    protected int itemsLeftToTransferLastSecond;
    private TickableSubscription subscription;
    private Widget ioModeSwitch;

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = 2 * (int) Math.pow(4, tier); // 8 32 128 512 1024
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.filterItem = ItemStack.EMPTY;
        this.io = IO.OUT;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
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
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSubscription));
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterItem.isEmpty()) {
            list.add(filterItem);
        }
        return list;
    }

    //////////////////////////////////////
    //*****     Transfer Logic     *****//
    //////////////////////////////////////

    public ItemFilter getFilterHandler() {
        if (filterHandler == null) {
            if (filterItem.isEmpty()) {
                return ItemFilter.EMPTY;
            } else {
                filterHandler = ItemFilter.loadFilter(filterItem);
            }
        }
        return filterHandler;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            updateSubscription();
        }
    }

    protected void updateSubscription() {
        var level = coverHolder.getLevel();
        var pos = coverHolder.getPos();
        if (isWorkingEnabled() && ItemTransferHelper.getItemTransfer(level, pos.relative(attachedSide), attachedSide.getOpposite()) != null) {
            subscription = coverHolder.subscribeServerTick(subscription, this::update);
        } else if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (itemsLeftToTransferLastSecond > 0) {
                var level = coverHolder.getLevel();
                var pos = coverHolder.getPos();
                var itemHandler = ItemTransferHelper.getItemTransfer(level, pos.relative(attachedSide), attachedSide.getOpposite());
                var myItemHandler = ItemTransferHelper.getItemTransfer(level, pos, attachedSide);
                if (itemHandler != null && myItemHandler != null) {
                    int totalTransferred = switch (io) {
                        case IN -> doTransferItems(itemHandler, myItemHandler, itemsLeftToTransferLastSecond);
                        case OUT -> doTransferItems(myItemHandler, itemHandler, itemsLeftToTransferLastSecond);
                        default -> 0;
                    };
                    this.itemsLeftToTransferLastSecond -= totalTransferred;
                }
            }
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
            updateSubscription();
        }
    }

    protected int doTransferItems(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        return moveInventoryItems(sourceInventory, targetInventory, maxTransferAmount);
    }

    protected int moveInventoryItems(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            if (!getFilterHandler().test(sourceStack)) {
                continue;
            }

            ItemStack remainder = ItemTransferHelper.insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    ItemTransferHelper.insertItem(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected static boolean moveInventoryItemsExact(IItemTransfer sourceInventory, IItemTransfer targetInventory, TypeItemInfo itemInfo) {
        //first, compute how much can we extract in reality from the machine,
        //because totalCount is based on what getStackInSlot returns, which may differ from what
        //extractItem() will return
        ItemStack resultStack = itemInfo.itemStack.copy();
        int totalExtractedCount = 0;
        int itemsLeftToExtract = itemInfo.totalCount;

        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.get(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, true);
            if (!extractedStack.isEmpty() &&
                    ItemStack.isSameItemSameTags(resultStack, extractedStack)) {
                totalExtractedCount += extractedStack.getCount();
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        //if amount of items extracted is not equal to the amount of items we
        //wanted to extract, abort item extraction
        if (totalExtractedCount != itemInfo.totalCount) {
            return false;
        }
        //adjust size of the result stack accordingly
        resultStack.setCount(totalExtractedCount);

        //now, see how much we can insert into destination inventory
        //if we can't insert as much as itemInfo requires, and remainder is empty, abort, abort
        ItemStack remainder = ItemTransferHelper.insertItem(targetInventory, resultStack, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        //otherwise, perform real insertion and then remove items from the source inventory
        ItemTransferHelper.insertItem(targetInventory, resultStack, false);

        //perform real extraction of the items from the source inventory now
        itemsLeftToExtract = itemInfo.totalCount;
        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.get(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, false);
            if (!extractedStack.isEmpty() &&
                    ItemStack.isSameItemSameTags(resultStack, extractedStack)) {
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        return true;
    }

    protected int moveInventoryItems(IItemTransfer sourceInventory, IItemTransfer targetInventory, Map<ItemStack, GroupItemInfo> itemInfos, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty() || !getFilterHandler().test(itemStack) || !itemInfos.containsKey(itemStack)) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(itemStack);

            ItemStack extractedStack = sourceInventory.extractItem(i, Math.min(itemInfo.totalCount, itemsLeftToTransfer), true);

            ItemStack remainderStack = ItemTransferHelper.insertItem(targetInventory, extractedStack, true);
            int amountToInsert = extractedStack.getCount() - remainderStack.getCount();

            if (amountToInsert > 0) {
                extractedStack = sourceInventory.extractItem(i, amountToInsert, false);

                if (!extractedStack.isEmpty()) {

                    ItemTransferHelper.insertItem(targetInventory, extractedStack, false);
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

    @Nonnull
    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(@Nonnull IItemTransfer inventory) {
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !getFilterHandler().test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new TypeItemInfo(s, new IntArrayList(), 0));

            itemInfo.totalCount += itemStack.getCount();
            itemInfo.slots.add(srcIndex);
        }

        return result;
    }

    @Nonnull
    protected Map<ItemStack, GroupItemInfo> countInventoryItemsByMatchSlot(@Nonnull IItemTransfer inventory) {
        Map<ItemStack, GroupItemInfo> result = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !getFilterHandler().test(itemStack)) {
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


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 135);

        var filterContainer = new ItemStackTransfer(filterItem);
        filterContainer.setFilter(itemStack -> ItemFilter.FILTERS.containsKey(itemStack.getItem()));
        var filterGroup = new WidgetGroup(0, 70, 176, 60);
        if (!filterItem.isEmpty()) {
            filterHandler = ItemFilter.loadFilter(filterItem);
            filterGroup.addWidget(filterHandler.openConfigurator(10, 0));
            configureFilterHandler();
        }

        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle(), GTValues.VN[tier])));

        group.addWidget(new IntInputWidget(10, 20, 156, 20, () -> this.transferRate, this::setTransferRate)
                .setMin(1).setMax(maxItemTransferRate));

        ioModeSwitch = new SwitchWidget(10, 45, 20, 20,
                (clickData, value) -> {
                    setIo(value ? IO.IN : IO.OUT);
                    ioModeSwitch.setHoverTooltips(
                            LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.localeName))
                    );
                })
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, IO.OUT.icon),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, IO.IN.icon))
                .setPressed(io == IO.IN)
                .setHoverTooltips(LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.localeName)));
        group.addWidget(ioModeSwitch);
        group.addWidget(new SlotWidget(filterContainer, 0, 148, 107)
                .setChangeListener(() -> {
                    if (isRemote()) {
                        if (!filterContainer.getStackInSlot(0).isEmpty() && !filterItem.isEmpty()) {
                            return;
                        }
                    }
                    this.filterItem = filterContainer.getStackInSlot(0);
                    this.filterHandler = null;
                    filterGroup.clearAllWidgets();
                    if (!filterItem.isEmpty()) {
                        filterHandler = ItemFilter.loadFilter(filterItem);
                        filterGroup.addWidget(filterHandler.openConfigurator(10, 0));
                        configureFilterHandler();
                    }
                })
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        group.addWidget(filterGroup);

        buildAdditionalUI(group);

        return group;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilterHandler() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }
}
