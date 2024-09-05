package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.common.pipelike.block.cable.CableBlock;
import com.gregtechceu.gtceu.common.pipelike.handlers.properties.MaterialEnergyProperties;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.WorldEnergyNet;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.*;

import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_1F;

public class CableInfoProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("cable_info");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        if (blockState.getBlock() instanceof CableBlock cableBlock) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(serverLevel).getNode(iProbeHitData.getPos());
            EnergyFlowLogic logic = node.getData().getLogicEntryNullable(EnergyFlowLogic.TYPE);

            long currentTick = serverLevel.getServer().getTickCount();
            long totalVoltage = 0L;
            double averageAmperage = logic.getAverageAmperage(currentTick);
            for (var flow : logic.getFlow(currentTick)) {
                totalVoltage += flow.voltage();
            }

            IProbeInfo horizontalPane = iProbeInfo
                    .horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.text(Component.translatable("gtceu.top.cable_voltage"));
            if (totalVoltage != 0) {
                horizontalPane.text(GTValues.VNF[GTUtil.getTierByVoltage(totalVoltage)]).text(" / ");
            }
            horizontalPane.text(
                    GTValues.VNF[GTUtil.getTierByVoltage(cableBlock.material.getProperty(PropertyKey.PIPENET_PROPERTIES)
                            .getProperty(MaterialEnergyProperties.KEY).getVoltageLimit())]);

            horizontalPane = iProbeInfo
                    .horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.text(Component.translatable("gtceu.top.cable_amperage"));
            if (averageAmperage != 0) {
                horizontalPane.text(DECIMAL_FORMAT_1F.format(averageAmperage) + "A / ");
            }
            horizontalPane.text(DECIMAL_FORMAT_1F.format(cableBlock.material.getProperty(PropertyKey.PIPENET_PROPERTIES)
                    .getProperty(MaterialEnergyProperties.KEY).getAmperage(cableBlock.getStructure())) + "A");
        }
    }
}
