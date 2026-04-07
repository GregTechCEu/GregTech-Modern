package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@NotNullByDefault
public class QuantumTankMachine extends TieredMachine implements IDropSaveMachine, IControllable,
                                IMuiMachine {

    public static Object2LongMap<MachineDefinition> TANK_CAPACITY = new Object2LongArrayMap<>();

    @SaveField
    @Getter
    @Setter
    private boolean isVoiding;

    @Getter
    private final long maxAmount;
    protected final FluidCache cache;
    @SyncToClient
    @SaveField
    private final CustomFluidTank lockedFluid;

    @Getter
    @SyncToClient
    @SaveField
    protected FluidStack stored = FluidStack.EMPTY;
    @Getter
    @SyncToClient
    @SaveField
    protected long storedAmount = 0;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public QuantumTankMachine(BlockEntityCreationInfo info, int tier, long maxAmount) {
        super(info, tier);
        this.maxAmount = maxAmount;
        this.cache = createCacheFluidHandler();
        this.lockedFluid = new CustomFluidTank(1000);
        this.autoOutput = AutoOutputTrait.ofFluids(this, cache);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected FluidCache createCacheFluidHandler() {
        return new FluidCache(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    protected void onFluidChanged() {
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

    @Override
    public boolean isWorkingEnabled() {
        return autoOutput.isAutoOutputFluids();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        autoOutput.setAllowAutoOutputFluids(isWorkingAllowed);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), cache)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUseWithItem(context);
    }

    public boolean isLocked() {
        return !lockedFluid.isEmpty();
    }

    protected void setLocked(boolean locked) {
        if (!stored.isEmpty() && locked) {
            var copied = new FluidStack(stored, 1000);
            lockedFluid.setFluid(copied);
        } else if (!locked) {
            lockedFluid.setFluid(FluidStack.EMPTY);
        }
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
    }

    protected void setLocked(FluidStack fluid) {
        if (fluid.isEmpty()) setLocked(false);
        else if (stored.isEmpty()) lockedFluid.setFluid(fluid);
        else if (stored.isFluidEqual(fluid)) setLocked(true);
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
    }

    public FluidStack getLockedFluid() {
        return lockedFluid.getFluid();
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.put("stored", stored.writeToNBT(new CompoundTag()));
        tag.putLong("storedAmount", storedAmount);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        stored = FluidStack.loadFluidStackFromNBT(tag.getCompound("stored"));
        storedAmount = tag.getLong("storedAmount");
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    // TODO: Needs AOI widget
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        LongSyncValue bucketSyncer = new LongSyncValue(this::getStoredAmount, (ignored) -> {});
        syncManager.syncValue("bucket_amount", bucketSyncer);

        mainWidget
                .child(new ParentWidget<>()
                        .background(GTGuiTextures.DISPLAY)
                        .size(90, 63)
                        .center()
                        .child(IKey.lang("gtceu.gui.fluid_amount").asWidget()
                                .color(0xffffff)
                                .margin(8, 0, 8, 0))
                        .child(IKey.dynamic(
                                () -> Component.literal(
                                        FormattingUtil.formatBuckets(bucketSyncer.getLongValue())))
                                .asWidget()
                                .color(0xffffff)
                                .margin(8, 0, 18, 0))
                        .child(Flow.row()
                                .margin(4, 0, 41, 0)
                                .coverChildren()
                                .child(GTMuiWidgets.createAutoOutputFluidButton(autoOutput))
                                .child(GTMuiWidgets.createToggleButton(this::isLocked, this::setLocked,
                                        GTGuiTextures.BUTTON_LOCK, "gtceu.gui.fluid_lock.tooltip"))
                                .child(GTMuiWidgets.createToggleButton(this::isVoiding, this::setVoiding,
                                        GTGuiTextures.BUTTON_VOID, "gtceu.gui.fluid_voiding_partial.tooltip")))
                        .child(Flow.column()
                                .margin(68, 0, 23, 0)
                                .coverChildren()
                                .child(createFluidSlot(syncManager))
                                .child(createPhantomLockedFluidSlot(syncManager))));
    }

    private IWidget createFluidSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("fluid_slot",
                SyncHandlers.fluidSlot(new FluidCacheTankWrapper(cache)).controlsAmount(false));
        return new FluidSlot().syncHandler("fluid_slot", 0).background(GTGuiTextures.FLUID_SLOT);
    }

    private IWidget createPhantomLockedFluidSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("locked_fluid_slot",
                new FluidSlotSyncHandler(lockedFluid).controlsAmount(false).phantom(true));
        return new FluidSlot().syncHandler("locked_fluid_slot", 0).background(GTGuiTextures.FLUID_SLOT);
    }

    protected class FluidCache extends MachineTrait implements IFluidHandler {

        public static final MachineTraitType<FluidCache> TYPE = new MachineTraitType<>(FluidCache.class);

        @Override
        public MachineTraitType<FluidCache> getTraitType() {
            return TYPE;
        }

        private final Predicate<FluidStack> filter = f -> !isLocked() || getLockedFluid().isFluidEqual(f);

        public FluidCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return new FluidStack(stored, GTMath.saturatedCast(storedAmount));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            long free = isVoiding ? Long.MAX_VALUE : maxAmount - storedAmount;
            long canFill = 0;
            if ((stored.isEmpty() || stored.isFluidEqual(resource)) && filter.test(resource)) {
                canFill = Math.min(resource.getAmount(), free);
            }
            if (action.execute() && canFill > 0) {
                if (stored.isEmpty()) stored = new FluidStack(resource, 1000);
                storedAmount = Math.min(maxAmount, storedAmount + canFill);
                onFluidChanged();
            }
            return (int) canFill;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (stored.isEmpty()) return FluidStack.EMPTY;
            long toDrain = Math.min(storedAmount, maxDrain);
            var copy = new FluidStack(stored, (int) toDrain);
            if (action.execute() && toDrain > 0) {
                storedAmount -= toDrain;
                if (storedAmount == 0) stored = FluidStack.EMPTY;
                onFluidChanged();
            }
            return copy.isEmpty() ? FluidStack.EMPTY : copy;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!resource.isFluidEqual(stored)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public int getTankCapacity(int tank) {
            return GTMath.saturatedCast(maxAmount);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return filter.test(stack);
        }

        public void exportToNearby(@NotNull Direction... facings) {
            if (stored.isEmpty()) return;
            var level = getMachine().getLevel();
            var pos = getMachine().getBlockPos();
            for (Direction facing : facings) {
                var filter = getMachine().getFluidCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentFluidHandler(level, pos, facing)
                        .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(this, adj, filter));
            }
        }
    }

    protected class FluidCacheTankWrapper implements IFluidTank {

        private final FluidCache cache;

        public FluidCacheTankWrapper(FluidCache cache) {
            this.cache = cache;
        }

        @Override
        public @NotNull FluidStack getFluid() {
            return cache.getFluidInTank(0);
        }

        @Override
        public int getFluidAmount() {
            return cache.getFluidInTank(0).getAmount();
        }

        @Override
        public int getCapacity() {
            return cache.getTankCapacity(0);
        }

        @Override
        public boolean isFluidValid(FluidStack fluidStack) {
            return cache.isFluidValid(0, fluidStack);
        }

        @Override
        public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            return cache.fill(fluidStack, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(int i, IFluidHandler.FluidAction fluidAction) {
            return cache.drain(i, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            return cache.drain(fluidStack, fluidAction);
        }
    }
}
