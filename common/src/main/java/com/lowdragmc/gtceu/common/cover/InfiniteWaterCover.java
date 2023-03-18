package com.lowdragmc.gtceu.common.cover;

import com.lowdragmc.gtceu.api.capability.ICoverable;
import com.lowdragmc.gtceu.api.cover.CoverBehavior;
import com.lowdragmc.gtceu.api.cover.CoverDefinition;
import com.lowdragmc.gtceu.api.machine.TickableSubscription;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote InfiniteWaterCover
 */
public class InfiniteWaterCover extends CoverBehavior {

    private TickableSubscription subscription;

    public InfiniteWaterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void update() {
        if (coverHolder.getOffsetTimer() % 20 == 0) {
            var fluidHandler = FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
            if(fluidHandler != null)
                fluidHandler.fill(FluidStack.create(Fluids.WATER, 16 * FluidHelper.getBucket()), false);
        }
    }

}
