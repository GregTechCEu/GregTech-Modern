package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControl;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControlProvider;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseHelpers;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.transfer.item.ItemTransferDelegate;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.cover.filter.MatchResult;
import com.gregtechceu.gtceu.common.cover.filter.MergabilityInfo;
import com.gregtechceu.gtceu.common.pipelike.net.item.*;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote ConveyorCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorCover extends CoverBehavior implements IUICover, IControllable,
                           TransferControlProvider, IItemTransferController {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConveyorCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);
    public final int tier;
    public final int maxItemTransferRate;
    @Persisted
    @Getter
    protected int transferRate;
    @Persisted
    @DescSynced
    @Getter
    @RequireRerender
    protected IO io;
    @Persisted
    @DescSynced
    @Getter
    protected DistributionMode distributionMode;
    @Persisted
    @DescSynced
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @Persisted
    @Getter
    protected boolean isWorkingEnabled = true;
    protected int itemsLeftToTransferLastSecond;
    private CoverableItemTransferWrapper itemHandlerWrapper;
    private Widget ioModeSwitch;

    protected final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> roundRobinCache = new Object2ObjectLinkedOpenHashMap<>();

    protected @Nullable CoverRenderer rendererInverted;

    @Persisted
    @DescSynced
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = 2 * (int) Math.pow(4, Math.min(tier, GTValues.LuV)); // 8 32 128 512 1024
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.io = IO.OUT;
        this.distributionMode = DistributionMode.INSERT_FIRST;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.item(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(f -> configureFilter());
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentItemTransfer() != null;
    }

    protected @Nullable IItemTransfer getOwnItemTransfer() {
        return coverHolder.getItemTransferCap(attachedSide, false);
    }

    protected @Nullable IItemTransfer getAdjacentItemTransfer() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos().relative(attachedSide),
                attachedSide.getOpposite());
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        coverHolder.markDirty();
    }

    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
        coverHolder.markDirty();
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        coverHolder.markDirty();
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
        if (timer % 5 == 0 && isWorkingEnabled && getItemsLeftToTransfer() > 0) {
            Direction side = attachedSide;
            BlockEntity tileEntity = coverHolder.getNeighbor(side);
            IItemHandler itemHandler = tileEntity == null ? null :
                    tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).resolve().orElse(null);
            IItemHandler myItemHandler = coverHolder.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve()
                    .orElse(null);
            if (itemHandler != null && myItemHandler != null) {
                IItemTransfer itemTransfer = ItemTransferHelperImpl.toItemTransfer(itemHandler);
                IItemTransfer myItemTransfer = ItemTransferHelperImpl.toItemTransfer(myItemHandler);
                if (io == IO.OUT) {
                    performTransferOnUpdate(myItemTransfer, itemTransfer);
                } else {
                    performTransferOnUpdate(itemTransfer, myItemTransfer);
                }
            }
        }
        if (timer % 20 == 0) {
            refreshBuffer(transferRate);
        }
    }

    protected int getItemsLeftToTransfer() {
        return itemsLeftToTransferLastSecond;
    }

    protected void reportItemsTransfer(int transferred) {
        this.itemsLeftToTransferLastSecond -= transferred;
    }

    protected void refreshBuffer(int transferRate) {
        this.itemsLeftToTransferLastSecond = transferRate;
    }

    protected void performTransferOnUpdate(@NotNull IItemTransfer sourceHandler, @NotNull IItemTransfer destHandler) {
        reportItemsTransfer(performTransfer(sourceHandler, destHandler, false, i -> 0,
                i -> getItemsLeftToTransfer(), null));
    }

    /**
     * Performs transfer
     *
     * @param sourceHandler  the handler to pull from
     * @param destHandler    the handler to push to
     * @param byFilterSlot   whether to perform the transfer by filter slot.
     * @param minTransfer    the minimum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param maxTransfer    the maximum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param transferReport where transfer is reported; a is the filter slot, b is the amount of transfer.
     *                       Each filter slot will report its transfer before the next slot is calculated.
     * @return how much was transferred in total.
     */
    protected int performTransfer(@NotNull IItemTransfer sourceHandler, @NotNull IItemTransfer destHandler,
                                  boolean byFilterSlot, @NotNull IntUnaryOperator minTransfer,
                                  @NotNull IntUnaryOperator maxTransfer,
                                  @Nullable BiConsumer<Integer, Integer> transferReport) {
        ItemFilter filter = filterHandler.getFilter();
        byFilterSlot = byFilterSlot && filter != ItemFilter.EMPTY; // can't be by filter slot if there is no filter
        Int2IntArrayMap extractableByFilterSlot = new Int2IntArrayMap();
        Int2ObjectArrayMap<MergabilityInfo<ItemTestObject>> filterSlotToMergability = new Int2ObjectArrayMap<>();
        for (int i = 0; i < sourceHandler.getSlots(); i++) {
            ItemStack stack = sourceHandler.extractItem(i, Integer.MAX_VALUE, true);
            int extracted = stack.getCount();
            if (extracted == 0) continue;
            MatchResult match = null;
            if (filter == ItemFilter.EMPTY || (match = filter.match(stack)).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    filterSlot = match.getFilterIndex();
                }
                extractableByFilterSlot.merge(filterSlot, extracted, Integer::sum);
                final int handlerSlot = i;
                filterSlotToMergability.compute(filterSlot, (k, v) -> {
                    if (v == null) v = new MergabilityInfo<>();
                    v.add(handlerSlot, new ItemTestObject(stack), extracted);
                    return v;
                });
            }
        }
        var iter = extractableByFilterSlot.int2IntEntrySet().fastIterator();
        int totalTransfer = 0;
        while (iter.hasNext()) {
            var next = iter.next();
            int filterSlot = next.getIntKey();
            int min = minTransfer.applyAsInt(filterSlot);
            int max = maxTransfer.applyAsInt(filterSlot);
            if (max < min || max <= 0) continue;
            int slotTransfer = 0;
            if (next.getIntValue() >= min) {
                MergabilityInfo<ItemTestObject> mergabilityInfo = filterSlotToMergability.get(filterSlot);
                MergabilityInfo<ItemTestObject>.Merge merge = mergabilityInfo.getLargestMerge();
                if (merge.getCount() >= min) {
                    int transfer = Math.min(merge.getCount(), max);
                    transfer = insertToHandler(destHandler, merge.getTestObject(), transfer, true);
                    // since we can't guarantee the insertability of multiple stack types while just simulating,
                    // if the largest merge is not large enough we have to give up.
                    if (transfer < min) continue;
                    int toExtract = transfer;
                    for (int handlerSlot : merge.getHandlerSlots()) {
                        toExtract -= sourceHandler.extractItem(handlerSlot, toExtract, false).getCount();
                        if (toExtract == 0) break;
                    }
                    insertToHandler(destHandler, merge.getTestObject(), transfer - toExtract, false);
                    int remaining = max - transfer + toExtract;
                    slotTransfer += transfer;
                    if (remaining <= 0) continue;
                    for (MergabilityInfo<ItemTestObject>.Merge otherMerge : mergabilityInfo
                            .getNonLargestMerges(merge)) {
                        transfer = Math.min(otherMerge.getCount(), remaining);
                        transfer = insertToHandler(destHandler, merge.getTestObject(), transfer, true);
                        toExtract = transfer;
                        for (int handlerSlot : otherMerge.getHandlerSlots()) {
                            toExtract -= sourceHandler.extractItem(handlerSlot, toExtract, false).getCount();
                            if (toExtract == 0) break;
                        }
                        insertToHandler(destHandler, otherMerge.getTestObject(), transfer - toExtract, false);
                        remaining -= transfer;
                        slotTransfer += transfer;
                        if (remaining <= 0) break;
                    }
                }
            }
            if (transferReport != null) transferReport.accept(filterSlot, slotTransfer);
            totalTransfer += slotTransfer;
        }
        return totalTransfer;
    }

    protected int insertToHandler(@NotNull IItemTransfer destHandler, ItemTestObject testObject, int count,
                                  boolean simulate) {
        if (!(destHandler instanceof IItemTraverseGuideProvider provider)) {
            return simpleInsert(destHandler, testObject, count, simulate);
        }
        switch (distributionMode) {
            case INSERT_FIRST -> {
                var guide = provider.getGuide(this::getTD, testObject, count, simulate);
                if (guide == null) return 0;
                int consumed = (int) TraverseHelpers.traverseFlood(guide.getData(), guide.getPaths(), guide.getFlow());
                guide.reportConsumedFlow(consumed);
                return consumed;
            }
            case ROUND_ROBIN_GLOBAL -> {
                var guide = provider.getGuide(this::getEQTD, testObject, count, simulate);
                if (guide == null) return 0;
                int consumed = (int) TraverseHelpers.traverseEqualDistribution(guide.getData(),
                        guide.getPathsSupplier(), guide.getFlow(), true);
                guide.reportConsumedFlow(consumed);
                return consumed;
            }
            case ROUND_ROBIN_PRIO -> {
                var guide = provider
                        .getGuide(
                                (net, testObject1, simulator, queryTick, sourcePos, inputFacing) -> getRRTD(net,
                                        testObject1, simulator, queryTick, sourcePos, inputFacing, simulate),
                                testObject, count, simulate);
                if (guide == null) return 0;
                int consumed = (int) TraverseHelpers.traverseRoundRobin(guide.getData(), guide.getPaths(),
                        guide.getFlow(), true);
                guide.reportConsumedFlow(consumed);
                return consumed;
            }
        }
        return 0;
    }

    @Contract("_, _, _, _, _, _ -> new")
    protected @NotNull ItemTraverseData getTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                              long queryTick, BlockPos sourcePos, Direction inputFacing) {
        return new ItemTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Contract("_, _, _, _, _, _ -> new")
    protected @NotNull ItemEQTraverseData getEQTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, Direction inputFacing) {
        return new ItemEQTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    protected @NotNull ItemRRTraverseData getRRTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, Direction inputFacing,
                                                  boolean simulate) {
        return new ItemRRTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing,
                getRoundRobinCache(simulate));
    }

    protected Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> getRoundRobinCache(boolean simulate) {
        return simulate ? roundRobinCache.clone() : roundRobinCache;
    }

    protected int simpleInsert(@NotNull IItemTransfer destHandler, ItemTestObject testObject, int count,
                               boolean simulate) {
        int available = count;
        for (int i = 0; i < destHandler.getSlots(); i++) {
            ItemStack toInsert = testObject.recombine(Math.min(available, destHandler.getSlotLimit(i)));
            available -= toInsert.getCount() - destHandler.insertItem(i, toInsert, simulate).getCount();
            if (available == 0) return count;
        }
        return count - available;
    }

    @Override
    public <T> @Nullable T getControllerForControl(TransferControl<T> control) {
        if (control == IItemTransferController.CONTROL) {
            return control.cast(this);
        }
        return null;
    }

    @Override
    public int insertToHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemTransfer destHandler,
                               boolean simulate) {
        if (getManualIOMode() == ManualIOMode.DISABLED) return amount;
        if (getManualIOMode() == ManualIOMode.UNFILTERED ||
                io == IO.IN) // insert to handler is an extract from us
            return IItemTransferController.super.insertToHandler(testObject, amount, destHandler, simulate);
        ItemFilter filter = getFilterHandler().getFilter();
        if (filter == ItemFilter.EMPTY || filter.test(testObject.recombine())) {
            return IItemTransferController.super.insertToHandler(testObject, amount, destHandler, simulate);
        } else return amount;
    }

    @Override
    public int extractFromHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemTransfer sourceHandler,
                                  boolean simulate) {
        if (getManualIOMode() == ManualIOMode.DISABLED) return 0;
        if (getManualIOMode() == ManualIOMode.UNFILTERED ||
                io == IO.OUT) // extract from handler is an insert to us
            return IItemTransferController.super.extractFromHandler(testObject, amount, sourceHandler, simulate);
        ItemFilter filter = getFilterHandler().getFilter();
        if (filter == ItemFilter.EMPTY || filter.test(testObject.recombine())) {
            return IItemTransferController.super.extractFromHandler(testObject, amount, sourceHandler, simulate);
        } else return 0;
    }

    protected static class TypeItemInfo {

        public final ItemStack itemStack;
        public final int filterSlot;
        public final IntList slots;
        public int totalCount;

        public TypeItemInfo(ItemStack itemStack, int filterSlot, IntList slots, int totalCount) {
            this.itemStack = itemStack;
            this.filterSlot = filterSlot;
            this.slots = slots;
            this.totalCount = totalCount;
        }
    }

    @NotNull
    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(@NotNull IItemTransfer inventory) {
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            var matchResult = getFilterHandler().getFilter().match(itemStack);
            if (!matchResult.isMatched()) continue;

            if (!result.containsKey(itemStack)) {
                TypeItemInfo itemInfo = new TypeItemInfo(itemStack.copy(), matchResult.getFilterIndex(),
                        new IntArrayList(), 0);
                itemInfo.totalCount += itemStack.getCount();
                itemInfo.slots.add(srcIndex);
                result.put(itemStack.copy(), itemInfo);
            } else {
                TypeItemInfo itemInfo = result.get(itemStack);
                itemInfo.totalCount += itemStack.getCount();
                itemInfo.slots.add(srcIndex);
            }
        }
        return result;
    }

    @Override
    public boolean canAttach(@NotNull ICoverable coverable, @NotNull Direction side) {
        return coverable.getCapability(ForgeCapabilities.ITEM_HANDLER, attachedSide).isPresent();
    }

    @Override
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (!coverHolder.getLevel().isClientSide) {
            createUI(playerIn);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, LazyOptional<T> defaultValue) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (!defaultValue.isPresent()) {
                return LazyOptional.empty();
            }
            IItemHandler delegate = (IItemHandler) defaultValue.resolve().orElse(null);
            if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != delegate) {
                this.itemHandlerWrapper = new CoverableItemTransferWrapper(
                        ItemTransferHelperImpl.toItemTransfer(delegate));
            }
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability,
                    LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(itemHandlerWrapper)));
        }
        if (capability == GTCapability.CAPABILITY_CONTROLLABLE) {
            return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return defaultValue;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 137);
        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle(), GTValues.VN[tier])));

        if (createThroughputRow()) {
            group.addWidget(new IntInputWidget(10, 20, 156, 20, () -> this.transferRate, this::setTransferRate)
                    .setMin(1).setMax(maxItemTransferRate));
        }

        if (createConveyorModeRow()) {
            ioModeSwitch = new SwitchWidget(10, 45, 20, 20,
                    (clickData, value) -> {
                        setIo(value ? IO.IN : IO.OUT);
                        ioModeSwitch.setHoverTooltips(
                                LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.tooltip)));
                    })
                    .setTexture(
                            new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, IO.OUT.icon),
                            new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, IO.IN.icon))
                    .setPressed(io == IO.IN)
                    .setHoverTooltips(
                            LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.tooltip)));
            group.addWidget(ioModeSwitch);
        }

        if (createDistributionModeRow()) {
            group.addWidget(new EnumSelectorWidget<>(146, 67, 20, 20,
                    DistributionMode.VALUES, distributionMode, this::setDistributionMode));
        }

        if (createManualIOModeRow()) {
            group.addWidget(new EnumSelectorWidget<>(146, 107, 20, 20,
                    ManualIOMode.VALUES, manualIOMode, this::setManualIOMode)
                    .setHoverTooltips("cover.universal.manual_import_export.mode.description"));
        }

        if (createFilterRow()) {
            group.addWidget(filterHandler.createFilterSlotUI(125, 108));
            group.addWidget(filterHandler.createFilterConfigUI(10, 72, 156, 60));
        }

        buildAdditionalUI(group);

        return group;
    }

    protected boolean createThroughputRow() {
        return true;
    }

    protected boolean createFilterRow() {
        return true;
    }

    protected boolean createManualIOModeRow() {
        return true;
    }

    protected boolean createConveyorModeRow() {
        return true;
    }

    protected boolean createDistributionModeRow() {
        return true;
    }

    protected int getMaxStackSize() {
        return 1;
    }

    @Override
    public @NotNull CoverRenderer getRenderer() {
        if (io == IO.OUT) {
            if (renderer == null) renderer = buildRenderer();
            return renderer;
        } else {
            if (rendererInverted == null) rendererInverted = buildRendererInverted();
            return rendererInverted;
        }
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_conveyor"),
                GTCEu.id("block/cover/overlay_conveyor_emissive")).build();
    }

    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_conveyor_inverted"),
                GTCEu.id("block/cover/overlay_conveyor_inverted_emissive")).build();
    }

    private class CoverableItemTransferWrapper extends ItemTransferDelegate {

        public CoverableItemTransferWrapper(IItemTransfer delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            if (io == IO.OUT && manualIOMode == ManualIOMode.DISABLED) {
                return stack;
            }
            if (manualIOMode == ManualIOMode.FILTERED && !filterHandler.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate, notifyChanges);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            if (io == IO.IN && manualIOMode == ManualIOMode.DISABLED) {
                return ItemStack.EMPTY;
            }
            if (manualIOMode == ManualIOMode.FILTERED) {
                ItemStack result = super.extractItem(slot, amount, true, notifyChanges);
                if (result.isEmpty() || !filterHandler.test(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false, notifyChanges);
            }
            return super.extractItem(slot, amount, simulate, notifyChanges);
        }
    }
}
