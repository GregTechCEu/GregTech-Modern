package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import com.gregtechceu.gtceu.api.capability.data.query.ComputationQuery;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkSwitchMachine extends DataBankMachine {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.IV];

    private long nextQueryTick;
    private ComputationQuery query;

    public NetworkSwitchMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected int calculateEnergyUsage() {
        int receivers = 0;
        int transmitters = 0;
        for (var part : this.getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) {
                ++receivers;
            }
            if (PartAbility.COMPUTATION_DATA_TRANSMISSION.isApplicable(block)) {
                ++transmitters;
            }
        }
        return EUT_PER_HATCH * (receivers + transmitters);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(getEnergyUsage())
                .addComputationUsageLine(queryConnected().maxCWUt())
                .addWorkingStatusLine();
    }

    /*
     * @Override
     * protected void addWarningText(List<Component> textList) {
     * super.addWarningText(textList);
     * if (isFormed() && computationHandler.hasNonBridgingConnections()) {
     * textList.add(Component.translatable("gtceu.multiblock.computation.non_bridging.detailed").withStyle(
     * ChatFormatting.YELLOW));
     * }
     * }
     */

    private ComputationQuery queryConnected() {
        long tick = Platform.getMinecraftServer().getTickCount();
        if (tick >= nextQueryTick) {
            this.query = new ComputationQuery();
            List<IDataAccess> dataAccesses = getParts().stream()
                    .filter(IDataAccess.class::isInstance)
                    .map(IDataAccess.class::cast)
                    .toList();
            IDataAccess.accessData(dataAccesses, query);
            this.nextQueryTick = tick + 10;
        }
        return this.query;
    }
}
