package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowData;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowLogic;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidFlowLogic;
import com.gregtechceu.gtceu.common.pipelike.net.item.ItemFlowLogic;
import com.gregtechceu.gtceu.integration.top.element.FluidStackElement;
import com.gregtechceu.gtceu.integration.top.element.FluidStyle;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.TextStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class PipeTileInfoProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("pipe_tile_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level,
                             BlockState state, IProbeHitData hitData) {
        if (state.getBlock() instanceof PipeBlock pipe) {
            PipeBlockEntity tile = pipe.getBlockEntity(level, hitData.getPos());
            if (tile != null) {
                for (NetLogicData data : tile.getNetLogicDatas().values()) {
                    EnergyFlowLogic energy = data.getLogicEntryNullable(EnergyFlowLogic.TYPE);
                    if (energy != null) {
                        addEnergyFlowInformation(mode, info, player, hitData, energy);
                    }
                    FluidFlowLogic fluid = data.getLogicEntryNullable(FluidFlowLogic.TYPE);
                    if (fluid != null) {
                        addFluidFlowInformation(mode, info, player, hitData, fluid);
                    }
                    ItemFlowLogic item = data.getLogicEntryNullable(ItemFlowLogic.TYPE);
                    if (item != null) {
                        addItemFlowInformation(mode, info, player, hitData, item);
                    }
                }
            }
        }
    }

    private void addEnergyFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, Player entityPlayer,
                                          IProbeHitData iProbeHitData, EnergyFlowLogic logic) {
        long cumulativeVoltage = 0;
        long cumulativeAmperage = 0;
        for (var memory : logic.getMemory().values()) {
            int count = 0;
            double voltage = 0;
            long amperage = 0;
            for (EnergyFlowData flow : memory) {
                count++;
                long prev = amperage;
                amperage += flow.amperage();
                // weighted average
                voltage = voltage * prev / amperage + (double) (flow.voltage() * flow.amperage()) / amperage;
            }
            if (count != 0) {
                cumulativeVoltage += voltage / count;
                cumulativeAmperage += amperage / count;
            }
        }
        iProbeInfo.text(CompoundText.create().info(Component.translatable("gtceu.top.pipe.voltage"))
                .important(Component.literal(String.valueOf(cumulativeVoltage / EnergyFlowLogic.MEMORY_TICKS))));
        iProbeInfo.text(CompoundText.create().info(Component.translatable("gtceu.top.pipe.amperage"))
                .important(Component.literal(String.valueOf(cumulativeAmperage / EnergyFlowLogic.MEMORY_TICKS))));
    }

    private void addFluidFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, Player entityPlayer,
                                         IProbeHitData iProbeHitData, FluidFlowLogic logic) {
        iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(CompoundText.create().info(Component.translatable("gtceu.top.pipe.fluid_last")))
                .element(new FluidStackElement(logic.getLast(), new FluidStyle()))
                .text(logic.getLast().getDisplayName());

        Object2LongOpenHashMap<FluidTestObject> counts = new Object2LongOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (FluidStack stack : memory) {
                counts.merge(new FluidTestObject(stack), stack.getAmount(), Long::sum);
            }
        }

        for (var entry : counts.object2LongEntrySet()) {
            FluidStack stack = entry.getKey().recombine();
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .element(new FluidStackElement(logic.getLast(), new FluidStyle().bounds(14, 14)))
                    .text(entry.getLongValue() * 20 / FluidFlowLogic.MEMORY_TICKS + " mB/S " + stack.getDisplayName().getString());
        }
    }

    private void addItemFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, Player entityPlayer,
                                        IProbeHitData iProbeHitData, ItemFlowLogic logic) {
        iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(CompoundText.create().info(Component.translatable("gtceu.top.pipe.item_last")))
                .item(logic.getLast())
                .text(logic.getLast().getDisplayName());

        Object2IntOpenHashMap<ItemTestObject> counts = new Object2IntOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (ItemStack stack : memory) {
                counts.merge(new ItemTestObject(stack), stack.getCount(), Integer::sum);
            }
        }

        for (var entry : counts.object2IntEntrySet()) {
            ItemStack stack = entry.getKey().recombine();
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .item(stack)
                    .text(entry.getIntValue() * 20 / ItemFlowLogic.MEMORY_TICKS + " /s " + stack.getDisplayName());
        }
    }
}
