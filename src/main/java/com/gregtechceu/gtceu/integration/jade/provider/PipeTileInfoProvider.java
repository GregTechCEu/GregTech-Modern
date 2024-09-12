package com.gregtechceu.gtceu.integration.jade.provider;

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
import com.gregtechceu.gtceu.integration.jade.element.FluidStackElement;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public class PipeTileInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("pipe_tile_provider");
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();
        if (serverData.contains("Energy")) {
            CompoundTag energy = serverData.getCompound("Energy");
            iTooltip.add(Component.translatable("gtceu.top.pipe.voltage")
                    .append(Component.literal(String.valueOf(energy.getLong("voltage")))));
            iTooltip.add(Component.translatable("gtceu.top.pipe.amperage")
                    .append(Component.literal(String.valueOf(energy.getLong("amperage")))));
        }
        if (serverData.contains("Fluid")) {
            CompoundTag fluid = serverData.getCompound("Fluid");

            FluidStack stack = FluidStack.loadFromTag(fluid.getCompound("LastFluid"));
            iTooltip.add(IElementHelper.get().text("gtceu.top.pipe.fluid_last").align(IElement.Align.LEFT));
            iTooltip.append(new FluidStackElement(stack, 14, 14));
            iTooltip.append(stack.getDisplayName());

            ListTag list = fluid.getList("ExtraFluids", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                stack = FluidStack.loadFromTag(tag.getCompound("Fluid"));
                iTooltip.add(new FluidStackElement(stack, 14, 14));
                iTooltip.append(Component.literal(String.valueOf(tag.getLong("Amount")))
                        .append(Component.literal(" mB/s "))
                        .append(stack.getDisplayName()));
            }
        }
        if (serverData.contains("Item")) {
            CompoundTag item = serverData.getCompound("Item");

            ItemStack stack = ItemStack.of(item.getCompound("LastItem"));
            iTooltip.add(IElementHelper.get().text("gtceu.top.pipe.item_last").align(IElement.Align.LEFT));
            iTooltip.append(IElementHelper.get().item(stack));
            iTooltip.append(stack.getDisplayName());

            ListTag list = item.getList("ExtraFluids", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                stack = ItemStack.of(tag.getCompound("Item"));
                iTooltip.add(IElementHelper.get().item(stack));
                iTooltip.append(Component.literal(String.valueOf(tag.getLong("Amount")))
                        .append(Component.literal(" /s "))
                        .append(stack.getDisplayName()));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlock() instanceof PipeBlock pipe) {
            PipeBlockEntity tile = pipe.getBlockEntity(blockAccessor.getLevel(), blockAccessor.getPosition());
            if (tile != null) {
                for (NetLogicData data : tile.getNetLogicDatas().values()) {
                    EnergyFlowLogic energy = data.getLogicEntryNullable(EnergyFlowLogic.TYPE);
                    if (energy != null) {
                        addEnergyFlowInformation(compoundTag, energy);
                    }
                    FluidFlowLogic fluid = data.getLogicEntryNullable(FluidFlowLogic.TYPE);
                    if (fluid != null) {
                        addFluidFlowInformation(compoundTag, fluid);
                    }
                    ItemFlowLogic item = data.getLogicEntryNullable(ItemFlowLogic.TYPE);
                    if (item != null) {
                        addItemFlowInformation(compoundTag, item);
                    }
                }
            }
        }
    }

    private void addEnergyFlowInformation(CompoundTag tag, EnergyFlowLogic logic) {
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
        CompoundTag energy = new CompoundTag();
        energy.putLong("voltage", cumulativeVoltage / EnergyFlowLogic.MEMORY_TICKS);
        energy.putLong("amperage", cumulativeAmperage / EnergyFlowLogic.MEMORY_TICKS);
        tag.put("Energy", energy);
    }

    private void addFluidFlowInformation(CompoundTag tag, FluidFlowLogic logic) {
        CompoundTag fluid = new CompoundTag();

        fluid.put("LastFluid", logic.getLast().saveToTag(new CompoundTag()));

        Object2LongOpenHashMap<FluidTestObject> counts = new Object2LongOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (FluidStack stack : memory) {
                counts.merge(new FluidTestObject(stack), stack.getAmount(), Long::sum);
            }
        }

        ListTag extraFluids = new ListTag();
        for (var entry : counts.object2LongEntrySet()) {
            CompoundTag inner = new CompoundTag();
            FluidStack stack = entry.getKey().recombine();

            inner.put("Fluid", stack.saveToTag(new CompoundTag()));
            inner.putLong("Amount", entry.getLongValue() * 20 / FluidFlowLogic.MEMORY_TICKS);
            extraFluids.add(inner);
        }
        fluid.put("ExtraFluids", extraFluids);
        tag.put("Fluid", fluid);
    }

    private void addItemFlowInformation(CompoundTag tag, ItemFlowLogic logic) {
        CompoundTag item = new CompoundTag();
        item.put("LastItem", logic.getLast().save(new CompoundTag()));

        Object2IntOpenHashMap<ItemTestObject> counts = new Object2IntOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (ItemStack stack : memory) {
                counts.merge(new ItemTestObject(stack), stack.getCount(), Integer::sum);
            }
        }

        ListTag extraItems = new ListTag();
        for (var entry : counts.object2IntEntrySet()) {
            CompoundTag inner = new CompoundTag();
            ItemStack stack = entry.getKey().recombine();

            inner.put("Item", stack.save(new CompoundTag()));
            inner.putInt("Amount", entry.getIntValue() * 20 / FluidFlowLogic.MEMORY_TICKS);
            extraItems.add(inner);
        }
        item.put("ExtraItems", extraItems);
        tag.put("Item", item);
    }
}
