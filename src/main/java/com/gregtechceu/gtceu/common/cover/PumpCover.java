package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseHelpers;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidTransferDelegate;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.cover.filter.MatchResult;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidEQTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidRRTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.IFluidTraverseGuideProvider;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntToLongFunction;
import java.util.function.LongUnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote PumpCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpCover extends CoverBehavior implements IUICover, IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PumpCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    protected static final long MILLIBUCKET_SIZE = FluidHelper.getBucket() / 1000;

    public final int tier;
    public final long maxMilliBucketsPerTick;

    @Persisted
    @DescSynced
    @Getter
    protected long currentMilliBucketsPerTick;
    @Persisted
    @DescSynced
    @Getter
    @RequireRerender
    protected IO io = IO.OUT;
    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected DistributionMode distributionMode = DistributionMode.INSERT_FIRST;
    @Persisted
    @DescSynced
    @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;
    @Persisted
    @DescSynced
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;

    @Persisted
    @Getter
    protected boolean isWorkingEnabled = true;
    protected long milliBucketsLeftToTransferLastSecond;

    @Persisted
    @DescSynced
    @Getter
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;
    private NumberInputWidget<Long> transferRateWidget;

    protected final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> roundRobinCache = new Object2ObjectLinkedOpenHashMap<>();

    protected @Nullable CoverRenderer rendererInverted;

    /*
     * Transfer rate variables are treated as liters/millibuckets per tick.
     * The actual conversion to the platform's values happens inside tick handling.
     */

    public PumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;

        this.maxMilliBucketsPerTick = 64 * (long) Math.pow(4, Math.min(tier - 1, GTValues.IV)); // .5b 2b 8b

        this.currentMilliBucketsPerTick = maxMilliBucketsPerTick;
        this.milliBucketsLeftToTransferLastSecond = currentMilliBucketsPerTick * 20;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.fluid(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(f -> configureFilter());
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentFluidTransfer() != null;
    }

    protected @Nullable IFluidTransfer getOwnFluidTransfer() {
        return coverHolder.getFluidTransferCap(attachedSide, false);
    }

    protected @Nullable IFluidTransfer getAdjacentFluidTransfer() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos().relative(attachedSide),
                attachedSide.getOpposite());
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach(@NotNull ICoverable coverable, @NotNull Direction side) {
        return coverable.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
        }
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

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        subscriptionHandler.updateSubscription();
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
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_pump"),
                GTCEu.id("block/cover/overlay_pump_emissive")).build();
    }

    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_pump_inverted"),
                GTCEu.id("block/cover/overlay_pump_inverted_emissive")).build();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    //////////////////////////////////////
    // ***** Transfer Logic *****//
    //////////////////////////////////////

    public void setTransferRate(long milliBucketsPerTick) {
        this.currentMilliBucketsPerTick = Math.min(Math.max(milliBucketsPerTick, 0), maxMilliBucketsPerTick);
    }

    public void setBucketMode(BucketMode bucketMode) {
        var oldMultiplier = this.bucketMode.multiplier;
        var newMultiplier = bucketMode.multiplier;

        this.bucketMode = bucketMode;

        if (transferRateWidget == null) return;

        if (oldMultiplier > newMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeTransferRate());
        }

        transferRateWidget.setMax(maxMilliBucketsPerTick / bucketMode.multiplier);

        if (newMultiplier > oldMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeTransferRate());
        }
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        coverHolder.markDirty();
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (isWorkingEnabled && getFluidsLeftToTransfer() > 0) {
            BlockEntity tileEntity = coverHolder.getNeighbor(attachedSide);
            IFluidHandler fluidHandlerCap = tileEntity == null ? null : tileEntity
                    .getCapability(ForgeCapabilities.FLUID_HANDLER, attachedSide.getOpposite()).resolve().orElse(null);
            IFluidHandler myFluidHandlerCap = coverHolder.getCapability(
                    ForgeCapabilities.FLUID_HANDLER,
                    attachedSide).resolve().orElse(null);
            if (myFluidHandlerCap != null && fluidHandlerCap != null) {
                IFluidTransfer myFluidHandler = FluidTransferHelperImpl.toFluidTransfer(myFluidHandlerCap);
                IFluidTransfer fluidHandler = FluidTransferHelperImpl.toFluidTransfer(fluidHandlerCap);
                if (io == IO.OUT) {
                    performTransferOnUpdate(myFluidHandler, fluidHandler);
                } else {
                    performTransferOnUpdate(fluidHandler, myFluidHandler);
                }
            }
        }
        if (timer % 20 == 0) {
            refreshBuffer(maxMilliBucketsPerTick);
        }

        subscriptionHandler.updateSubscription();
    }

    public long getFluidsLeftToTransfer() {
        return milliBucketsLeftToTransferLastSecond;
    }

    public void reportFluidsTransfer(long transferred) {
        milliBucketsLeftToTransferLastSecond -= transferred;
    }

    protected void refreshBuffer(long transferRate) {
        this.milliBucketsLeftToTransferLastSecond = transferRate;
    }

    protected void performTransferOnUpdate(@NotNull IFluidTransfer sourceHandler, @NotNull IFluidTransfer destHandler) {
        reportFluidsTransfer(performTransfer(sourceHandler, destHandler, false, i -> 0,
                i -> getFluidsLeftToTransfer(), null));
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
    protected long performTransfer(@NotNull IFluidTransfer sourceHandler, @NotNull IFluidTransfer destHandler,
                                   boolean byFilterSlot, @NotNull IntToLongFunction minTransfer,
                                   @NotNull IntToLongFunction maxTransfer,
                                   @Nullable BiConsumer<Integer, Long> transferReport) {
        FluidFilter filter = this.filterHandler.getFilter();
        byFilterSlot = byFilterSlot && filter != FluidFilter.EMPTY; // can't be by filter slot if there is no filter
        Object2LongOpenHashMap<FluidTestObject> contained = new Object2LongOpenHashMap<>();
        for (int i = 0; i < sourceHandler.getTanks(); ++i) {
            FluidStack contents = sourceHandler.getFluidInTank(i);
            if (!contents.isEmpty()) contained.merge(new FluidTestObject(contents), contents.getAmount(), Long::sum);
        }
        var iter = contained.object2LongEntrySet().fastIterator();
        long totalTransfer = 0;
        while (iter.hasNext()) {
            var content = iter.next();
            FluidStack contents = content.getKey().recombine(content.getLongValue());
            MatchResult match = null;
            if (filter == FluidFilter.EMPTY || (match = filter.match(contents)).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    assert filter != FluidFilter.EMPTY; // we know it is not null, because if it were byFilterSlot would
                                                        // be false.
                    filterSlot = match.getFilterIndex();
                }
                long min = minTransfer.applyAsLong(filterSlot);
                long max = maxTransfer.applyAsLong(filterSlot);
                if (max < min || max <= 0) continue;

                if (contents.getAmount() < min) continue;
                long transfer = Math.min(contents.getAmount(), max);
                FluidStack extracted = sourceHandler.drain(content.getKey().recombine(transfer), false);
                if (extracted == null || extracted.getAmount() < min) continue;
                transfer = insertToHandler(destHandler, content.getKey(), extracted.getAmount(), true);
                if (transfer <= 0 || transfer < min) continue;
                extracted = sourceHandler.drain(content.getKey().recombine(transfer), true);
                if (extracted == null) continue;
                transfer = insertToHandler(destHandler, content.getKey(), extracted.getAmount(), false);
                if (transferReport != null) transferReport.accept(filterSlot, transfer);
                totalTransfer += transfer;
            }
        }
        return totalTransfer;
    }

    protected long insertToHandler(@NotNull IFluidTransfer destHandler, FluidTestObject testObject, long count,
                                   boolean simulate) {
        if (!(destHandler instanceof IFluidTraverseGuideProvider provider)) {
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
    protected @NotNull FluidTraverseData getTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                               long queryTick, BlockPos sourcePos, Direction inputFacing) {
        return new FluidTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Contract("_, _, _, _, _, _ -> new")
    protected @NotNull FluidEQTraverseData getEQTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                                   long queryTick, BlockPos sourcePos, Direction inputFacing) {
        return new FluidEQTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    protected @NotNull FluidRRTraverseData getRRTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                                   long queryTick, BlockPos sourcePos, Direction inputFacing,
                                                   boolean simulate) {
        return new FluidRRTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing,
                getRoundRobinCache(simulate));
    }

    protected Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> getRoundRobinCache(boolean simulate) {
        return simulate ? roundRobinCache.clone() : roundRobinCache;
    }

    protected long simpleInsert(@NotNull IFluidTransfer destHandler, FluidTestObject testObject, long count,
                                boolean simulate) {
        return count - destHandler.fill(testObject.recombine(count), !simulate);
    }

    protected boolean checkInputFluid(FluidStack fluidStack) {
        return filterHandler.test(fluidStack);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 137);
        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle(), GTValues.VN[tier])));

        transferRateWidget = new LongInputWidget(10, 20, 134, 20,
                this::getCurrentBucketModeTransferRate, this::setCurrentBucketModeTransferRate).setMin(0L);
        setBucketMode(this.bucketMode); // initial input widget config happens here
        group.addWidget(transferRateWidget);

        group.addWidget(new EnumSelectorWidget<>(
                146, 20, 20, 20,
                Arrays.stream(BucketMode.values()).filter(m -> m.multiplier <= maxMilliBucketsPerTick).toList(),
                bucketMode, this::setBucketMode).setTooltipSupplier(this::getBucketModeTooltip));

        group.addWidget(new EnumSelectorWidget<>(10, 45, 20, 20, List.of(IO.IN, IO.OUT), io, this::setIo));

        group.addWidget(new EnumSelectorWidget<>(146, 107, 20, 20,
                ManualIOMode.VALUES, manualIOMode, this::setManualIOMode)
                .setHoverTooltips("cover.universal.manual_import_export.mode.description"));

        group.addWidget(filterHandler.createFilterSlotUI(125, 108));
        group.addWidget(filterHandler.createFilterConfigUI(10, 72, 156, 60));

        buildAdditionalUI(group);

        return group;
    }

    private List<Component> getBucketModeTooltip(BucketMode mode, String langKey) {
        return List.of(
                Component.translatable(langKey).append(Component.translatable("gtceu.gui.content.units.per_tick")));
    }

    private long getCurrentBucketModeTransferRate() {
        return this.currentMilliBucketsPerTick / this.bucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferRate(long transferRate) {
        this.setTransferRate(transferRate * this.bucketMode.multiplier);
    }

    @NotNull
    protected String getUITitle() {
        return "cover.pump.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /////////////////////////////////////

    private CoverableFluidTransferWrapper fluidTransferWrapper;

    @Nullable
    @Override
    public IFluidTransfer getFluidTransferCap(@Nullable IFluidTransfer defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (fluidTransferWrapper == null || fluidTransferWrapper.delegate != defaultValue) {
            this.fluidTransferWrapper = new CoverableFluidTransferWrapper(defaultValue);
        }
        return fluidTransferWrapper;
    }

    private class CoverableFluidTransferWrapper extends FluidTransferDelegate {

        public CoverableFluidTransferWrapper(IFluidTransfer delegate) {
            super(delegate);
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            if (io == IO.OUT && manualIOMode == ManualIOMode.DISABLED) {
                return 0;
            }
            if (!filterHandler.test(resource) && manualIOMode == ManualIOMode.FILTERED) {
                return 0;
            }
            return super.fill(tank, resource, simulate, notifyChanges);
        }

        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            if (io == IO.IN && manualIOMode == ManualIOMode.DISABLED) {
                return FluidStack.empty();
            }
            if (manualIOMode == ManualIOMode.FILTERED && !filterHandler.test(resource)) {
                return FluidStack.empty();
            }
            return super.drain(tank, resource, simulate, notifyChanges);
        }
    }
}
