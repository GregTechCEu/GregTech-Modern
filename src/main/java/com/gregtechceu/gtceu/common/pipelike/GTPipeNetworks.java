package com.gregtechceu.gtceu.common.pipelike;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GTPipeNetworks {

    public static final PipeNetworkType ENERGY = new PipeNetworkType(GTCEu.id("energy"), GTBlockEntities.CABLE::get);

    public static final PipeNetworkType DUCT = new PipeNetworkType(GTCEu.id("duct"), GTBlockEntities.DUCT_PIPE::get);

    public static final PipeNetworkType FLUID = new PipeNetworkType(GTCEu.id("fluid"), GTBlockEntities.FLUID_PIPE::get);

    public static final PipeNetworkType ITEM = new PipeNetworkType(GTCEu.id("item"), GTBlockEntities.ITEM_PIPE::get);

    public static final PipeNetworkType LASER = new PipeNetworkType(GTCEu.id("laser"), GTBlockEntities.LASER_PIPE::get);

    public static final PipeNetworkType OPTICAL = new PipeNetworkType(GTCEu.id("optical"),
            GTBlockEntities.OPTICAL_PIPE::get);
}
