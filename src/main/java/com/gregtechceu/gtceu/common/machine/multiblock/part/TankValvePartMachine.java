package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.FluidTankProxyTrait;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.MultiblockTankMachine;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TankValvePartMachine extends MultiblockPartMachine {

    private FluidTankProxyTrait tankProxy;
    private ConditionalSubscriptionHandler autoIOSubscription;
    private ISubscription tankChangeListener;

    public TankValvePartMachine(IMachineBlockEntity holder, boolean isMetal, Object... args) {
        super(holder);

        tankProxy = createTank(args);
        autoIOSubscription = new ConditionalSubscriptionHandler(this, this::autoIO, this::shouldAutoIO);
    }

    protected FluidTankProxyTrait createTank(Object... args) {
        return new FluidTankProxyTrait(this, IO.BOTH);
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        autoIOSubscription.updateSubscription();
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);

        if (controller instanceof MultiblockTankMachine multiblockTank) {
            tankProxy.setProxy(multiblockTank.getTank());
            unsubscribeChanges();
            tankChangeListener = multiblockTank.getTank().addChangedListener(autoIOSubscription::updateSubscription);
        }
        autoIOSubscription.updateSubscription();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);

        tankProxy.setProxy(null);
        autoIOSubscription.updateSubscription();
        unsubscribeChanges();
    }

    private void unsubscribeChanges() {
        if (tankChangeListener != null)
            tankChangeListener.unsubscribe();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        autoIOSubscription.updateSubscription();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        autoIOSubscription.updateSubscription();
    }

    @Nullable
    private IFluidHandler getTargetTank() {
        return GTTransferUtils.getAdjacentFluidHandler(getLevel(), getPos(), getFrontFacing()).resolve().orElse(null);
    }

    private void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            tankProxy.exportToNearby(getFrontFacing());
        }

        autoIOSubscription.updateSubscription();
    }

    private boolean shouldAutoIO() {
        if (!isFormed()) return false;
        if (getFrontFacing() != Direction.DOWN) return false;
        if (tankProxy.isEmpty()) return false;
        if (getTargetTank() == null) return false;

        return true;
    }
}
