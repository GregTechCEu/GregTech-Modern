package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;

    public FluidHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, long initialCapacity) {
        super(holder, tier, io);
        this.tank = createTank(initialCapacity);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createTank(long initialCapacity) {
        return new NotifiableFluidTank(this, 1, getTankCapacity(initialCapacity), io);
    }

    protected long getTankCapacity(long initialCapacity) {
        return initialCapacity * (1L << Math.min(9, getTier()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTankSubscription));
        }
        tankSubs = tank.addChangedListener(this::updateTankSubscription);
    }

    private void upgradeToMultiTanks() {

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
        var group = new WidgetGroup(0, 0, 89, 63);
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> String.valueOf(tank.getFluidInTank(0).getAmount())).setTextColor(-1).setDropShadow(true))
                .addWidget(new TankWidget(tank.storages[0], 67, 22, true, io.support(IO.IN))
                        .setBackground(GuiTextures.FLUID_SLOT));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }
}
