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
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
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
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote PumpCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpCover extends CoverBehavior implements IUICover, IControllable {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PumpCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);
    public final int tier;
    public final long maxFluidTransferRate;
    @Persisted @Getter
    protected long transferRate;

    @Persisted @DescSynced @Getter @RequireRerender
    protected IO io = IO.OUT;
    @Persisted @DescSynced @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;

    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    protected long fluidLeftToTransferLastSecond;

    @Persisted @DescSynced
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public PumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = FluidHelper.getBucket() / 8 * (long) Math.pow(4, tier + 1); // .5b 2b 8b
        this.transferRate = maxFluidTransferRate;
        this.fluidLeftToTransferLastSecond = transferRate;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.fluid(this).onFilterLoaded(f -> configureFilterHandler());
    }

    private boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentFluidTransfer() != null;
    }

    protected @Nullable IFluidTransfer getOwnFluidTransfer() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
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

    public void setTransferRate(long transferRate) {
        if (transferRate <= maxFluidTransferRate) {
            this.transferRate = transferRate;
        }
    }

    public void setBucketMode(BucketMode bucketMode) {
        this.bucketMode = bucketMode;
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

    //////////////////////////////////////
    //*****     Transfer Logic     *****//
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

    private void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (fluidLeftToTransferLastSecond > 0) {
                this.fluidLeftToTransferLastSecond -= doTransferFluids(fluidLeftToTransferLastSecond);
            }
            if (timer % 20 == 0) {
                this.fluidLeftToTransferLastSecond = transferRate;
            }
            subscriptionHandler.updateSubscription();
        }
    }

    protected long doTransferFluids(long transferLimit) {
        var adjustedTransferLimit = transferLimit * bucketMode.multiplier;

        var adjacentFluidTransfer = getAdjacentFluidTransfer();
        var ownFluidTransfer = getOwnFluidTransfer();
        if (adjacentFluidTransfer == null || ownFluidTransfer == null) {
            return 0;
        }

        return switch (io) {
            case IN -> doTransferFluidsInternal(adjacentFluidTransfer, ownFluidTransfer, adjustedTransferLimit);
            case OUT -> doTransferFluidsInternal(ownFluidTransfer, adjacentFluidTransfer, adjustedTransferLimit);
            default -> 0L;
        };
    }

    protected long doTransferFluidsInternal(IFluidTransfer source, IFluidTransfer destination, long transferLimit) {
        return FluidTransferHelper.transferFluids(source, destination, transferLimit, filterHandler.getFilter());
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 135);
        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle(), GTValues.VN[tier])));

        group.addWidget(new LongInputWidget(10, 20, 156, 20, this::getTransferRate, this::setTransferRate)
                .setMin(1L).setMax(maxFluidTransferRate));

        group.addWidget(new EnumSelectorWidget<>(10, 45, 20, 20, List.of(IO.IN, IO.OUT), io, this::setIo));
        group.addWidget(new EnumSelectorWidget<>(35, 45, 20, 20, BucketMode.values(), bucketMode, this::setBucketMode));

        group.addWidget(filterHandler.createFilterSlotUI(148, 107));
        group.addWidget(filterHandler.createFilterConfigUI(10, 70, 156, 60));

        buildAdditionalUI(group);

        return group;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.pump.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilterHandler() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }
}
