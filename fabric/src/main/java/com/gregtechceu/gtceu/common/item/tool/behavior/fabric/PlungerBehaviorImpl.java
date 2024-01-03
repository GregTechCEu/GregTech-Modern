package com.gregtechceu.gtceu.common.item.tool.behavior.fabric;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.misc.fabric.VoidFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.item.tool.behavior.PlungerBehavior;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class PlungerBehaviorImpl extends PlungerBehavior {
    public static PlungerBehavior create() {
        return new PlungerBehaviorImpl();
    }

    @Override
    public void init(IGTTool toolItem) {
        FluidStorage.ITEM.registerForItems((itemStack, context) -> new VoidFluidHandlerItemStack(context, FluidHelper.getBucket()) {
            @Override
            public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                long result = super.insert(resource, maxAmount, transaction);
                if (result > 0) {
                    ToolHelper.damageItem(itemStack, null);
                }
                return result;
            }
        }, toolItem.asItem());
    }
}
