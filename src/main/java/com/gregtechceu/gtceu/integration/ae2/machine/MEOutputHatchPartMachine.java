package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputHatchPartMachine extends MEHatchPartMachine
                                      implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEOutputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private KeyStorage internalBuffer; // Do not use KeyCounter, use our simple implementation

    public MEOutputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    @NotNull
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.internalBuffer = new KeyStorage();
        this.internalBuffer.setOnContentsChanged(this::onChanged);
        return new InaccessibleInfiniteTank(this);
    }

    @Override
    @NotNull
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(10, 15, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEListGridWidget.Fluid(16, 25, 3, this.internalBuffer));

        return group;
    }

    @Override
    protected void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            var grid = getMainNode().getGrid();
            if (grid != null && !internalBuffer.storage.isEmpty()) {
                internalBuffer.insertInventory(grid.getStorageService().getInventory(), actionSource);
            }
            this.updateTankSubscription();
        }
    }

    @Override
    protected void updateTankSubscription() {
        if (isWorkingEnabled() && !internalBuffer.storage.isEmpty() && this.getLevel() != null &&
                GridHelper.getNodeHost(getLevel(), getPos().relative(getFrontFacing())) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class InaccessibleInfiniteTank extends NotifiableFluidTank {

        public InaccessibleInfiniteTank(MetaMachine holder) {
            super(holder, 0, 0, IO.OUT);
        }

//        @Override
//        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
//                                                       @Nullable String slotName, boolean simulate) {
//            return handleIngredient(io, recipe, left, simulate, this.handlerIO,
//                    Stream.generate(() -> new FluidStorage(0) {
//
//                        @Override
//                        public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
//                            return InaccessibleInfiniteTank.this.fill(resource, simulate, notifyChanges);
//                        }
//                    }).limit(this.internalBuffer.size()).toArray(FluidStorage[]::new));
//        }
//
//        private class FluidStorageDelegate extends FluidStorage {
//
//            public FluidStorageDelegate() {
//                super(0L);
//            }
//
//            @Override
//            public FluidStack getFluid() {
//                return FluidStack.empty();
//            }
//
//            @Override
//            public void setFluid(FluidStack fluid) {
//                // NO-OP
//            }
//
//            @Override
//            public long getFluidAmount() {
//                return fluid.getAmount();
//            }
//
//            @Override
//            public long getCapacity() {
//                // Its capacity is always 0.
//                return 0;
//            }
//
//            @Override
//            public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
//                return 0;
//            }
//
//            @Override
//            public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
//                return 0;
//            }
//
//            @Override
//            public boolean supportsFill(int tank) {
//                return false;
//            }
//
//            @Override
//            public FluidStack drain(
//                    int tank, FluidStack resource, boolean simulate, boolean notifyChange) {
//                if (tank == 0) {
//                    return drain(resource, simulate, notifyChange);
//                }
//                return FluidStack.empty();
//            }
//
//            @Override
//            public FluidStack drain(FluidStack resource, boolean doDrain, boolean notifyChanges) {
//                var config = fluid.getConfig();
//                if (!resource.isEmpty() && config != null && AEUtils.matches(config, resource)) {
//                    return this.drain(resource.getAmount(), doDrain, notifyChanges);
//                }
//                return FluidStack.empty();
//            }
//
//            @Override
//            public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
//                return fluid.drain(maxDrain, simulate);
//            }
//
//            @Override
//            public boolean supportsDrain(int tank) {
//                return tank == 0;
//            }
//
//            @Override
//            public boolean isFluidValid(FluidStack stack) {
//                return false;
//            }
//
//            @Override
//            public FluidStorage copy() {
//                return new FluidStorage(getFluid());
//            }
//        }
    }
}
