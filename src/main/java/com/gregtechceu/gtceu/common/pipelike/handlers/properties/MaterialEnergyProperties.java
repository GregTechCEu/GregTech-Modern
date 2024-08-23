package com.gregtechceu.gtceu.common.pipelike.handlers.properties;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PipeNetProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.ThroughputLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLossFunction;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.common.pipelike.block.cable.CableStructure;
import com.gregtechceu.gtceu.common.pipelike.block.pipe.MaterialPipeStructure;
import com.gregtechceu.gtceu.common.pipelike.net.energy.SuperconductorLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.VoltageLimitLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.VoltageLossLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.WorldEnergyNet;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;

import java.util.List;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FOIL;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.NO_UNIFICATION;

public final class MaterialEnergyProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    public static final MaterialPropertyKey<MaterialEnergyProperties> KEY = new MaterialPropertyKey<>(
            "EnergyProperties");

    private final long voltageLimit;
    private final long amperageLimit;
    private int materialMeltTemperature;
    private final long lossPerAmp;
    private final int superconductorCriticalTemperature;

    /**
     * Generate a MaterialEnergyProperties
     * 
     * @param voltageLimit                      the voltage limit for the cable
     * @param amperageLimit                     the base amperage for the cable.
     * @param lossPerAmp                        the base loss per amp per block traveled.
     * @param superconductorCriticalTemperature the superconductor temperature. When the temperature is at or below
     *                                          superconductor temperature, loss will be treated as zero. A
     *                                          superconductor
     *                                          temperature of 0 or less will be treated as not a superconductor.
     */
    public MaterialEnergyProperties(long voltageLimit, long amperageLimit, long lossPerAmp,
                                    int superconductorCriticalTemperature) {
        this.voltageLimit = voltageLimit;
        this.amperageLimit = amperageLimit;
        this.lossPerAmp = lossPerAmp;
        this.superconductorCriticalTemperature = superconductorCriticalTemperature;
    }

    public long getVoltageLimit() {
        return voltageLimit;
    }

    public static MaterialEnergyProperties create(long voltageLimit, long amperageLimit, long lossPerAmp,
                                                  int superconductorCriticalTemperature) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp,
                superconductorCriticalTemperature);
    }

    public static MaterialEnergyProperties create(long voltageLimit, long amperageLimit, long lossPerAmp) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, 0);
    }

    public static TagPrefix.MaterialRecipeHandler<MaterialEnergyProperties> registrationHandler(TagPrefix.MaterialRecipeHandler<MaterialEnergyProperties> handler) {
        return (orePrefix, material, properties, provider) -> {
            if (material.hasProperty(PropertyKey.PIPENET_PROPERTIES) && !material.hasFlag(NO_UNIFICATION) &&
                    material.getProperty(PropertyKey.PIPENET_PROPERTIES).hasProperty(KEY)) {
                handler.accept(orePrefix, material,
                        material.getProperty(PropertyKey.PIPENET_PROPERTIES).getProperty(KEY), provider);
            }
        };
    }

    public boolean isSuperconductor() {
        return this.superconductorCriticalTemperature > 1;
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, BlockGetter worldIn, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flagIn, IPipeMaterialStructure structure) {
        int tier = GTUtil.getTierByVoltage(voltageLimit);
        if (isSuperconductor())
            tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage", voltageLimit, GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage", getAmperage(structure)));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block", getLoss(structure)));
        if (isSuperconductor())
            tooltip.add(Component.translatable("gtceu.cable.superconductor_loss", superconductorCriticalTemperature));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor() && voltageLimit >= GTValues.V[GTValues.IV] &&
                    !thisMaterial.hasFlag(GENERATE_FOIL)) {
                thisMaterial.addFlags(GENERATE_FOIL);
            }
        }
        this.materialMeltTemperature = computeMaterialMeltTemperature(properties);
    }

    public static int computeMaterialMeltTemperature(@NotNull MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.FLUID)) {
            // autodetermine melt temperature from registered fluid
            FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
            Fluid fluid = prop.get(FluidStorageKeys.LIQUID);
            if (fluid == null) {
                FluidBuilder builder = prop.getQueuedBuilder(FluidStorageKeys.LIQUID);
                if (builder != null) {
                    return builder.getDeterminedTemperature(properties.getMaterial(), FluidStorageKeys.LIQUID);
                }
            } else {
                return fluid.getFluidType().getTemperature();
            }
        }
        return 3000;
    }

    @Override
    @Nullable
    public WorldPipeNetNode getOrCreateFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure) {
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        } else if (structure instanceof MaterialPipeStructure pipe) {
            long amperage = amperageLimit * pipe.material() / 2;
            if (amperage == 0) return null; // skip pipes that are too small
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), pipe);
            return node;
        }
        return null;
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            long loss = getLoss(structure);
            long amperage = getAmperage(structure);
            boolean insulated = cable.partialBurnStructure() != null;
            // insulated cables cool down half as fast
            float coolingFactor = (float) (Math.sqrt(cable.material()) / (insulated ? 8 : 4));
            data.setLogicEntry(VoltageLossLogic.INSTANCE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.INSTANCE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.INSTANCE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.INSTANCE
                            .getWith(TemperatureLossFunction.getOrCreateCable(coolingFactor), materialMeltTemperature,
                                    1,
                                    100 * cable.material(), cable.partialBurnThreshold()));
            if (superconductorCriticalTemperature > 0) {
                data.setLogicEntry(SuperconductorLogic.INSTANCE.getWith(superconductorCriticalTemperature));
            }
        } else if (structure instanceof MaterialPipeStructure pipe) {
            long amperage = getAmperage(structure);
            if (amperage == 0) return; // skip pipes that are too small
            long loss = getLoss(structure);
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            data.setLogicEntry(VoltageLossLogic.INSTANCE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.INSTANCE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.INSTANCE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.INSTANCE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), materialMeltTemperature, 1,
                                    50 * pipe.material(), null));
            if (superconductorCriticalTemperature > 0) {
                data.setLogicEntry(SuperconductorLogic.INSTANCE.getWith(superconductorCriticalTemperature));
            }
        }
    }

    private long getLoss(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return lossPerAmp * cable.costFactor();
        } else if (structure instanceof MaterialPipeStructure pipe) {
            return lossPerAmp * (pipe.material() > 6 ? 3 : 2);
        } else return lossPerAmp;
    }

    private long getAmperage(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return amperageLimit * cable.material();
        } else if (structure instanceof MaterialPipeStructure pipe) {
            return amperageLimit * pipe.material() / 2;
        } else return amperageLimit;
    }

    @Override
    @Nullable
    public WorldPipeNetNode getFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof MaterialPipeStructure)
            return WorldEnergyNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(ServerLevel world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof MaterialPipeStructure) {
            WorldEnergyNet net = WorldEnergyNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure instanceof CableStructure cable && (!isSuperconductor() || !cable.isInsulated());
    }

    @Override
    public boolean supportsStructure(IPipeStructure structure) {
        return structure instanceof CableStructure || structure instanceof MaterialPipeStructure;
    }
}
