package com.gregtechceu.gtceu.common.pipelike.handlers.properties;

import com.gregtechceu.gtceu.api.capability.IPropertyFluidFilter;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PipeNetProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.ThroughputLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLossFunction;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.common.pipelike.block.pipe.MaterialPipeStructure;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidContainmentLogic;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.WorldFluidNet;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MaterialFluidProperties implements PipeNetProperties.IPipeNetMaterialProperty, IPropertyFluidFilter {

    public static final MaterialPropertyKey<MaterialFluidProperties> KEY = new MaterialPropertyKey<>("FluidProperties");

    @Getter
    private final Set<FluidAttribute> containedAttributes = new ObjectOpenHashSet<>();
    private final EnumSet<FluidState> containableStates = EnumSet.of(FluidState.LIQUID);

    @Getter
    private final int maxFluidTemperature;
    private final int minFluidTemperature;
    private int materialMeltTemperature;

    private final long baseThroughput;
    private final float priority;

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature,
                                   float priority) {
        this.baseThroughput = baseThroughput;
        this.maxFluidTemperature = maxFluidTemperature;
        this.minFluidTemperature = minFluidTemperature;
        this.priority = priority;
    }

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature) {
        this(baseThroughput, maxFluidTemperature, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature) {
        return createMax(baseThroughput, maxFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, maxFluidTemperature,
                FluidConstants.CRYOGENIC_FLUID_THRESHOLD + 1, priority);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature) {
        return createMin(baseThroughput, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, minFluidTemperature, priority);
    }

    public static MaterialFluidProperties create(long baseThroughput) {
        return create(baseThroughput, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties create(long baseThroughput, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, 0, priority);
    }

    public MaterialFluidProperties setContain(FluidState state, boolean canContain) {
        if (canContain) contain(state);
        else notContain(state);
        return this;
    }

    public MaterialFluidProperties setContain(FluidAttribute attribute, boolean canContain) {
        if (canContain) contain(attribute);
        else notContain(attribute);
        return this;
    }

    public MaterialFluidProperties contain(FluidState state) {
        this.containableStates.add(state);
        return this;
    }

    public MaterialFluidProperties contain(FluidAttribute attribute) {
        this.containedAttributes.add(attribute);
        return this;
    }

    public MaterialFluidProperties notContain(FluidState state) {
        this.containableStates.remove(state);
        return this;
    }

    public MaterialFluidProperties notContain(FluidAttribute attribute) {
        this.containedAttributes.remove(attribute);
        return this;
    }

    public boolean canContain(@NotNull FluidState state) {
        return this.containableStates.contains(state);
    }

    public boolean canContain(@NotNull FluidAttribute attribute) {
        return this.containedAttributes.contains(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        setContain(attribute, canContain);
    }

    public int getMinFluidTemperature() {
        return minFluidTemperature;
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flagIn, IPipeMaterialStructure structure) {
        tooltip.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", getThroughput(structure)));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature", getMaxFluidTemperature()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.min_temperature", getMinFluidTemperature()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.priority",
                FormattingUtil.formatNumbers(getFlowPriority(structure))));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }
        this.materialMeltTemperature = MaterialEnergyProperties.computeMaterialMeltTemperature(properties);
    }

    @Override
    @Nullable
    public WorldPipeNetNode getOrCreateFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldPipeNetNode node = WorldFluidNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        }
        return null;
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            long throughput = getThroughput(structure);
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            data.setLogicEntry(WeightFactorLogic.TYPE.getWith(getFlowPriority(structure)))
                    .setLogicEntry(ThroughputLogic.TYPE.getWith(throughput))
                    .setLogicEntry(FluidContainmentLogic.TYPE.getWith(containableStates, containedAttributes,
                            maxFluidTemperature))
                    .setLogicEntry(TemperatureLogic.TYPE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), materialMeltTemperature,
                                    minFluidTemperature, 50 * pipe.material(), null));
        }
    }

    private long getThroughput(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return baseThroughput * pipe.material();
        } else return baseThroughput;
    }

    private double getFlowPriority(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return priority * (pipe.restrictive() ? 100d : 1d) * pipe.channelCount() / pipe.material();
        } else return priority;
    }

    @Override
    public @Nullable WorldPipeNetNode getFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure)
            return WorldFluidNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldFluidNet net = WorldFluidNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure.getClass() == MaterialPipeStructure.class;
    }

    @Override
    public boolean supportsStructure(IPipeStructure structure) {
        return structure instanceof MaterialPipeStructure;
    }
}
