package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote FluidHatchPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidHatchPartMachine extends TieredIOPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    public static final long INITIAL_TANK_CAPACITY_1X = 8 * FluidHelper.getBucket();
    public static final long INITIAL_TANK_CAPACITY_4X = 2 * FluidHelper.getBucket();
    public static final long INITIAL_TANK_CAPACITY_9X = FluidHelper.getBucket();

    @Persisted
    public final NotifiableFluidTank tank;
    private final int slots;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;

    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public FluidHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, long initialCapacity, int slots, Object... args) {
        super(holder, tier, io);
        this.slots = slots;
        this.tank = createTank(initialCapacity, slots, args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        return new NotifiableFluidTank(this, slots, getTankCapacity(initialCapacity, getTier()), io);
    }

    public static long getTankCapacity(long initialCapacity, int tier) {
        return initialCapacity * (1L << Math.min(9, tier));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTankSubscription));
        }
        tankSubs = tank.addChangedListener(this::updateTankSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
    }

    //////////////////////////////////////
    //********     Auto IO     *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateTankSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateTankSubscription();
    }

    protected void updateTankSubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && !tank.isEmpty()) || io == IO.IN)
                && FluidTransferHelper.getFluidTransfer(getLevel(), getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    tank.exportToNearby(getFrontFacing());
                } else if (io == IO.IN){
                    tank.importFromNearby(getFrontFacing());
                }
            }
            updateTankSubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateTankSubscription();
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        if (slots == 1) {
            return createSingleSlotGUI();
        } else {
            return createMultiSlotGUI();
        }
    }

    protected Widget createSingleSlotGUI() {
        var group = new WidgetGroup(0, 0, 89, 63);
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY));
        TankWidget tankWidget;

        // Add input/output-specific widgets
        if (this.io == IO.OUT) {
            tankWidget = new PhantomFluidWidget(this.tank.getLockedFluid(), 67, 41, 18, 18)
                .setIFluidStackUpdater(f -> {
                    if (this.tank.getStorages()[0].getFluidAmount() != 0) {
                        return;
                    }
                    if (f.isEmpty()) {
                        this.tank.setLocked(false);
                        this.tank.getLockedFluid().setFluid(FluidStack.empty());
                    } else {
                        this.tank.setLocked(true);
                        this.tank.getLockedFluid().setFluid(f.copy());
                        this.tank.getLockedFluid().getFluid().setAmount(1);
                    }
                }).setShowAmount(true).setDrawHoverTips(false);

            group.addWidget(new ToggleButtonWidget(7, 41, 18, 18,
                    GuiTextures.BUTTON_LOCK, this.tank::isLocked, this.tank::setLocked)
                    .setTooltipText("gtceu.gui.fluid_lock.tooltip")
                    .setShouldUseBaseBackground());
        } else {
            tankWidget = new TankWidget(tank.getStorages()[0], 69, 52, 18, 18, true, io.support(IO.IN))
                .setShowAmount(true).setDrawHoverTips(false);

            group.addWidget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON));
        }

        group.addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
            .addWidget(new LabelWidget(8, 18, () -> getFluidAmountText(tankWidget)))
            .addWidget(new LabelWidget(8, 28, () -> getFluidNameText(tankWidget).getString()))
            .addWidget(tankWidget)
            .addWidget(new TankWidget(tank.getStorages()[0], 67, 22, true, io.support(IO.IN)).setBackground(GuiTextures.FLUID_SLOT));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private Component getFluidNameText(TankWidget tankWidget) {
        Component translation;
        if (tankWidget.getFluidTank() == null && !tankWidget.getFluidTank().getFluidInTank(tankWidget.getTank()).isEmpty()) {
            translation = tankWidget.getFluidTank().getFluidInTank(tankWidget.getTank()).getDisplayName();
        } else {
            translation = this.tank.getLockedFluid().getFluid().getDisplayName();
        }
        return translation;
    }

    private String getFluidAmountText(TankWidget tankWidget) {
        String fluidAmount = "";
        if (tankWidget.getFluidTank() != null && !tankWidget.getFluidTank().getFluidInTank(tankWidget.getTank()).isEmpty()) {
            fluidAmount = getFormattedFluidAmount(tankWidget.getFluidTank().getFluidInTank(tankWidget.getTank()));
        } else {
            // Display Zero to show information about the locked fluid
            if (!this.tank.getLockedFluid().getFluid().isEmpty()) {
                fluidAmount = "0";
            }
        }
        return fluidAmount;
    }

    public String getFormattedFluidAmount(FluidStack fluidStack) {
        return String.format("%,d", fluidStack.isEmpty() ? 0 : fluidStack.getAmount());
    }

    protected Widget createMultiSlotGUI() {
        int rowSize = (int) Math.sqrt(slots);
        int colSize = rowSize;
        if (slots == 8) {
            rowSize = 4;
            colSize = 2;
        }

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(new TankWidget(tank.getStorages()[index++], 4 + x * 18, 4 + y * 18, true, io.support(IO.IN)).setBackground(GuiTextures.FLUID_SLOT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
