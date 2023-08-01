package com.gregtechceu.gtceu.common.cover;

import com.google.common.math.LongMath;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;

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
    protected IO io;
    @Persisted @Getter
    protected boolean bucketMode;
    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    @Persisted @DescSynced @Getter
    protected ItemStack filterItem;
    @Nullable
    private FluidFilter filterHandler;
    protected long fluidLeftToTransferLastSecond;

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public PumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = FluidHelper.getBucket() / 8 * (long) Math.pow(4, tier); // .5b 2b 8b
        this.transferRate = maxFluidTransferRate;
        this.fluidLeftToTransferLastSecond = transferRate;
        this.filterItem = ItemStack.EMPTY;
        this.io = IO.OUT;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
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

    public void setBucketMode(boolean bucketMode) {
        this.bucketMode = bucketMode;
        if (this.bucketMode)
            setTransferRate(transferRate / FluidHelper.getBucket() * FluidHelper.getBucket());
    }


    protected void adjustTransferRate(long amount) {
        amount *= this.bucketMode ? FluidHelper.getBucket() : 1;
        setTransferRate(Mth.clamp(transferRate + amount, 1, maxFluidTransferRate));
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
        if (!filterItem.isEmpty()) {
            list.add(filterItem);
        }
        return list;
    }

    //////////////////////////////////////
    //*****     Transfer Logic     *****//
    //////////////////////////////////////

    public Predicate<FluidStack> getFilterHandler() {
        if (filterHandler == null) {
            if (filterItem.isEmpty()) {
                return itemStack -> true;
            } else {
                filterHandler = FluidFilter.loadFilter(filterItem);
            }
        }
        return filterHandler;
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
        var fluidHandler = getAdjacentFluidTransfer();
        var myFluidHandler = getOwnFluidTransfer();
        if (fluidHandler == null || myFluidHandler == null) {
            return 0;
        }
        return doTransferFluidsInternal(myFluidHandler, fluidHandler, transferLimit);
    }

    protected long doTransferFluidsInternal(IFluidTransfer myFluidHandler, IFluidTransfer fluidHandler, long transferLimit) {
        if (io == IO.IN) {
            return FluidTransferHelper.transferFluids(fluidHandler, myFluidHandler, transferLimit, getFilterHandler());
        } else if (io == IO.OUT) {
            return FluidTransferHelper.transferFluids(myFluidHandler, fluidHandler, transferLimit, getFilterHandler());
        }
        return 0;
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 135);
        var filterContainer = new ItemStackTransfer(filterItem);
        filterContainer.setFilter(itemStack -> FluidFilter.FILTERS.containsKey(itemStack.getItem()));
        var filterGroup = new WidgetGroup(0, 70, 176, 60);
        if (!filterItem.isEmpty()) {
            filterHandler = FluidFilter.loadFilter(filterItem);
            filterGroup.addWidget(filterHandler.openConfigurator((176 - 80) / 2, (60 - 55) / 2));
        }
        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format("cover.pump.title", GTValues.VN[tier])));
        group.addWidget(new ButtonWidget(10, 20, 30, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("-1")), cd -> {
            if (!cd.isRemote) {
                adjustTransferRate(-(cd.isCtrlClick ? cd.isShiftClick ? 1000 : 100 : cd.isShiftClick ? 10 : 1));
            }
        }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));
        group.addWidget(new ButtonWidget(136, 20, 30, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("+1")), cd -> {
            if (!cd.isRemote) {
                adjustTransferRate(cd.isCtrlClick ? cd.isShiftClick ? 1000 : 100 : cd.isShiftClick ? 10 : 1);
            }
        }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));
        group.addWidget(new TextFieldWidget(42, 20, 92, 20, () -> bucketMode ? Long.toString(transferRate / FluidHelper.getBucket()) : Long.toString(transferRate), val -> {
            var amount = Long.parseLong(val);
            if (this.bucketMode) {
                amount = LongMath.saturatedMultiply(amount, FluidHelper.getBucket());
            }
            setTransferRate(amount);
        }).setNumbersOnly(1L, maxFluidTransferRate));
        group.addWidget(new SwitchWidget(10, 45, 75, 20, (clickData, value) -> {
            if (!clickData.isRemote) {
                setIo(value ? IO.IN : IO.OUT);
            }
        }).setTexture(new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.pump.mode.export")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.pump.mode.import")))
                .setPressed(io == IO.IN));
        group.addWidget(new SwitchWidget(85, 45, 75, 20, (clickData, value) -> {
            if (!clickData.isRemote) {
                setBucketMode(value);
            }
        }).setTexture(new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.bucket.mode.milli_bucket")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.bucket.mode.bucket")))
                .setPressed(isBucketMode()));
        group.addWidget(new SlotWidget(filterContainer, 0, 10, 70)
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
                        filterHandler = FluidFilter.loadFilter(filterItem);
                        filterGroup.addWidget(filterHandler.openConfigurator((176 - 80) / 2, (60 - 55) / 2));
                    }
                })
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        group.addWidget(filterGroup);
        return group;
    }
}
