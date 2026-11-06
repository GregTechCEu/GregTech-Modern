package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.fluids.GTFluid;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.LiquidBlock;

public class GTLiquidBlock extends LiquidBlock {

    public GTLiquidBlock(GTFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public String getDescriptionId() {
        return this.fluid.getFluidType().getDescriptionId();
    }

    @Override
    public MutableComponent getName() {
        return this.fluid.getFluidType().getDescription().copy();
    }
}
