package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.FluidTankProxyTrait;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyTrait;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CokeOvenHatch extends MultiblockPartMachine {

    public final ItemHandlerProxyTrait inputInventory, outputInventory;
    public final FluidTankProxyTrait tank;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription outputInventorySubs, outputTankSubs;

    public CokeOvenHatch(BlockEntityCreationInfo info) {
        super(info);
        this.inputInventory = attachTrait(new ItemHandlerProxyTrait(IO.IN));
        this.outputInventory = attachTrait(new ItemHandlerProxyTrait(IO.OUT));
        this.tank = attachTrait(new FluidTankProxyTrait(IO.BOTH));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onUnload() {
        super.onUnload();
        inputInventory.setProxy(null);
        outputInventory.setProxy(null);
        tank.setProxy(null);
        if (outputInventorySubs != null) {
            outputInventorySubs.unsubscribe();
            outputInventorySubs = null;
        }
        if (outputTankSubs != null) {
            outputTankSubs.unsubscribe();
            outputTankSubs = null;
        }
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        super.addedToController(controller);
        if (controller instanceof CokeOvenMachine cokeOven) {
            outputInventorySubs = cokeOven.exportItems.addChangedListener(this::updateAutoIOSubscription);
            outputTankSubs = cokeOven.exportFluids.addChangedListener(this::updateAutoIOSubscription);
            inputInventory.setProxy(cokeOven.importItems);
            outputInventory.setProxy(cokeOven.exportItems);
            tank.setProxy(cokeOven.exportFluids);
            this.updateAutoIOSubscription();
        }
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        inputInventory.setProxy(null);
        outputInventory.setProxy(null);
        tank.setProxy(null);
        if (outputInventorySubs != null) {
            outputInventorySubs.unsubscribe();
            outputInventorySubs = null;
        }
        if (outputTankSubs != null) {
            outputTankSubs.unsubscribe();
            outputTankSubs = null;
        }
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public boolean replacePartModelWhenFormed() {
        return false;
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoIOSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateAutoIOSubscription();
    }

    protected void updateAutoIOSubscription() {
        if ((!outputInventory.isEmpty() &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), getFrontFacing())) ||
                (!tank.isEmpty() &&
                        GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), getFrontFacing()))) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            outputInventory.exportToNearby(getFrontFacing());
            tank.exportToNearby(getFrontFacing());
            updateAutoIOSubscription();
        }
    }

    //////////////////////////////////////
    // ********* GUI *********//
    //////////////////////////////////////
    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }
}
