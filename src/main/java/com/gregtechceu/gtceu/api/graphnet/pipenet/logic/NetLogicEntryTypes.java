package com.gregtechceu.gtceu.api.graphnet.pipenet.logic;

import com.gregtechceu.gtceu.api.graphnet.logic.ChannelCountLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.MultiNetCountLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.ThroughputLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.SuperconductorLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.VoltageLimitLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.VoltageLossLogic;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidContainmentLogic;

public final class NetLogicEntryTypes {

    public static void init() {
        Object type = ChannelCountLogic.TYPE;
        type = MultiNetCountLogic.TYPE;
        type = ThroughputLogic.TYPE;
        type = WeightFactorLogic.TYPE;
        type = EdgeCoverReferenceLogic.TYPE;
        type = TemperatureLogic.TYPE;
        type = EnergyFlowLogic.TYPE;
        type = SuperconductorLogic.TYPE;
        type = VoltageLimitLogic.TYPE;
        type = VoltageLossLogic.TYPE;
        type = FluidContainmentLogic.TYPE;
    }
}
