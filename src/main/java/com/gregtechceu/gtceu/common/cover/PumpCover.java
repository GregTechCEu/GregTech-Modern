package com.gregtechceu.gtceu.common.cover;

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
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidTransferDelegate;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote PumpCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpCover extends CoverBehavior implements IUICover, IControllable {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PumpCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    protected static final long MILLIBUCKET_SIZE = FluidHelper.getBucket() / 1000;

    public final int tier;
    public final long maxMilliBucketsPerTick;

    @Persisted @DescSynced @Getter
    protected long currentMilliBucketsPerTick;
    @Persisted @DescSynced @Getter @RequireRerender
    protected IO io = IO.OUT;
    @Persisted @DescSynced @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;
    @Persisted @DescSynced @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;

    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    protected long milliBucketsLeftToTransferLastSecond;

    @Persisted @DescSynced
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;
    private NumberInputWidget<Long> transferRateWidget;

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
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos().relative(attachedSide), attachedSide.getOpposite());
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
        return getOwnFluidTransfer() != null;
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
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    //////////////////////////////////////
    //*****     Transfer Logic     *****//
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
        if (timer % 5 != 0)
            return;

        if (milliBucketsLeftToTransferLastSecond > 0) {
            long platformTransferredFluid = doTransferFluids(milliBucketsLeftToTransferLastSecond * MILLIBUCKET_SIZE);
            this.milliBucketsLeftToTransferLastSecond -= platformTransferredFluid / MILLIBUCKET_SIZE;
        }

        if (timer % 20 == 0) {
            this.milliBucketsLeftToTransferLastSecond = currentMilliBucketsPerTick * 20;
        }

        subscriptionHandler.updateSubscription();
    }

    private long doTransferFluids(long platformTransferLimit) {
        var adjacentFluidTransfer = getAdjacentFluidTransfer();
        var ownFluidTransfer = getOwnFluidTransfer();

        if (adjacentFluidTransfer != null && ownFluidTransfer != null) {
            return switch (io) {
                case IN -> doTransferFluidsInternal(adjacentFluidTransfer, ownFluidTransfer, platformTransferLimit);
                case OUT -> doTransferFluidsInternal(ownFluidTransfer, adjacentFluidTransfer, platformTransferLimit);
                default -> 0L;
            };
        }
        return 0;

    }

    protected long doTransferFluidsInternal(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        return transferAny(source, destination, platformTransferLimit);
    }

    protected long transferAny(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        return FluidTransferHelper.transferFluids(source, destination, platformTransferLimit, filterHandler.getFilter());
    }


    protected enum TransferDirection {
        INSERT,
        EXTRACT
    }

    protected Map<FluidStack, Long> enumerateDistinctFluids(IFluidTransfer fluidTransfer, TransferDirection direction) {
        final Map<FluidStack, Long> summedFluids = new Object2LongOpenCustomHashMap<>(FluidStackHashStrategy.comparingAllButAmount());

        for (int tank = 0; tank < fluidTransfer.getTanks(); tank++) {
            if (!canTransfer(fluidTransfer, direction, tank))
                continue;

            FluidStack fluidStack = fluidTransfer.getFluidInTank(tank);
            if (fluidStack.isEmpty())
                continue;

            summedFluids.putIfAbsent(fluidStack, 0L);
            summedFluids.computeIfPresent(fluidStack, (stack, totalAmount) -> {
                return totalAmount + stack.getAmount();
            });
        }

        return summedFluids;
    }

    private static boolean canTransfer(IFluidTransfer fluidTransfer, TransferDirection direction, int tank) {
        return switch (direction) {
            case INSERT -> fluidTransfer.supportsFill(tank);
            case EXTRACT -> fluidTransfer.supportsDrain(tank);
        };
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
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
                bucketMode, this::setBucketMode
        ).setTooltipSupplier(this::getBucketModeTooltip));

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
                Component.translatable(langKey).append(Component.translatable("gtceu.gui.content.units.per_tick"))
        );
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
    //***    CAPABILITY OVERRIDE    ***//
    /////////////////////////////////////

    private final Map<Direction, IFluidTransfer> fluidTransferWrappers = new EnumMap<>(Direction.class);

    @Override
    public IFluidTransfer getFluidTransferCap(Direction side, IFluidTransfer defaultValue) {
        return fluidTransferWrappers.computeIfAbsent(side, s -> new CoverableFluidTransferWrapper(defaultValue));
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
