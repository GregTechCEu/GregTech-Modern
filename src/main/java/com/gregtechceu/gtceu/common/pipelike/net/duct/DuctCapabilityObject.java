package com.gregtechceu.gtceu.common.pipelike.net.duct;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.graphnet.pipenet.BasicWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class DuctCapabilityObject implements IPipeCapabilityObject, IHazardParticleContainer {

    private final WorldPipeNet net;
    @Setter
    private @Nullable PipeBlockEntity tile;

    public <N extends WorldPipeNet & BasicWorldPipeNetPath.Provider> DuctCapabilityObject(@NotNull N net) {
        this.net = net;
    }

    private BasicWorldPipeNetPath.Provider getProvider() {
        return (BasicWorldPipeNetPath.Provider) net;
    }

    private Iterator<BasicWorldPipeNetPath> getPaths() {
        assert tile != null;
        long tick = Platform.getMinecraftServer().getTickCount();
        return getProvider().getPaths(net.getNode(tile.getBlockPos()), IPredicateTestObject.INSTANCE, null, tick);
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldDuctNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_HAZARD_CONTAINER) {
            return GTCapability.CAPABILITY_HAZARD_CONTAINER.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return null;
    }

    @Override
    public boolean inputsHazard(Direction side, MedicalCondition condition) {
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                IHazardParticleContainer container = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, capability.getKey().getOpposite()).resolve()
                        .orElse(null);
                if (container != null && container.inputsHazard(side, condition)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float changeHazard(MedicalCondition condition, float differenceAmount) {
        float total = 0;
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                IHazardParticleContainer handler = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, capability.getKey().getOpposite()).resolve()
                        .orElse(null);
                if (handler == null) {
                    if (net.getLevel().getBlockEntity(path.getTargetNode().getEquivalencyData()
                            .relative(capability.getKey())) instanceof IMachineBlockEntity machineBE &&
                            machineBE.getMetaMachine() instanceof IEnvironmentalHazardCleaner cleaner) {
                        cleaner.cleanHazard(condition, differenceAmount);
                        break;
                    }

                    var savedData = EnvironmentalHazardSavedData.getOrCreate((ServerLevel) net.getLevel());
                    savedData.addZone(path.getTargetNode().getEquivalencyData().relative(capability.getKey()),
                            differenceAmount, true, HazardProperty.HazardTrigger.INHALATION, condition);
                    total += differenceAmount;
                    emitPollutionParticles((ServerLevel) net.getLevel(), path.getTargetNode().getEquivalencyData(), capability.getKey());
                    break;
                }
                float change = handler.changeHazard(condition, differenceAmount);
                differenceAmount -= change;
                total += change;
                if (differenceAmount <= 0) {
                    break;
                }
            }
        }
        return total;
    }

    @Override
    public float getHazardStored(MedicalCondition condition) {
        float total = 0;
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                IHazardParticleContainer handler = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, capability.getKey().getOpposite()).resolve()
                        .orElse(null);
                if (handler != null) {
                    total += handler.getHazardStored(condition);
                }
            }
        }
        return total;
    }

    @Override
    public float getHazardCapacity(MedicalCondition condition) {
        float total = 0;
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                IHazardParticleContainer handler = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, capability.getKey().getOpposite()).resolve()
                        .orElse(null);
                if (handler != null) {
                    total += handler.getHazardCapacity(condition);
                }
            }
        }
        return total;
    }

    public static void emitPollutionParticles(ServerLevel level, BlockPos pos, Direction frontFacing) {
        float xPos = frontFacing.getStepX() * 0.76F + pos.getX() + 0.25F;
        float yPos = frontFacing.getStepY() * 0.76F + pos.getY() + 0.25F;
        float zPos = frontFacing.getStepZ() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = frontFacing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd;
        float zSpd;

        if (frontFacing.getStepY() == -1) {
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = frontFacing.getStepX() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
            zSpd = frontFacing.getStepZ() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                xPos + GTValues.RNG.nextFloat() * 0.5F,
                yPos + GTValues.RNG.nextFloat() * 0.5F,
                zPos + GTValues.RNG.nextFloat() * 0.5F,
                1,
                xSpd, ySpd, zSpd,
                0.1);
    }
}
