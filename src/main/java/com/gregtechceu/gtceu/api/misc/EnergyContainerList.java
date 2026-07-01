package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;

import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class EnergyContainerList{

    public static final EnergyContainerList EMPTY = new EnergyContainerList(List.of());

    private static final boolean ALLOW_VOLTAGE_BOOST = true;

    private final IEnergyContainer[] energyContainers;

    @Getter
    private final long totalEUt;

    @Getter
    private final long effectiveVoltage;

    /** The highest single energy container's input voltage in the list. */
    @Getter
    private final long highestVoltage;

    @Getter
    private final int tier;

    public EnergyContainerList(List<? extends IEnergyContainer> energyContainerList) {
        this.energyContainers = energyContainerList.toArray(new IEnergyContainer[]{});
        long totalInputEUt = 0;
        long totalOutputEUt = 0;
        long highestVoltage = 0;
        int numHighestInputContainers = 1;
        for (IEnergyContainer container : energyContainers) {
            long inputVoltage = container.getInputVoltage();
            long outputVoltage = container.getOutputVoltage();

            if(inputVoltage != 0) {
                totalInputEUt += inputVoltage * container.getInputAmperage();
                if (inputVoltage > highestVoltage) {
                    highestVoltage = container.getInputVoltage();
                    numHighestInputContainers = 1;
                } else if (inputVoltage == highestVoltage) {
                    numHighestInputContainers += 1;
                }
            } else if(outputVoltage != 0) {
                totalOutputEUt += outputVoltage * container.getOutputAmperage();
                if(outputVoltage > highestVoltage) {
                    highestVoltage = outputVoltage;
                }
            }
        }

        this.totalEUt = totalInputEUt > 0 ? totalInputEUt : totalOutputEUt;
        this.effectiveVoltage = totalEUt > 0 ? GTValues.VEX[GTUtil.getFloorTierByVoltage(totalEUt)] : 0;
        this.highestVoltage = highestVoltage;
        int highestTier = GTUtil.getFloorTierByVoltage(highestVoltage);
        int maxOverClockTier = GTUtil.getFloorTierByVoltage(effectiveVoltage);
        if(!ALLOW_VOLTAGE_BOOST || highestTier >= maxOverClockTier
                || numHighestInputContainers == 1 || highestTier >= GTValues.MAX) {
            this.tier = highestTier;
        } else {
            this.tier = highestTier + 1;
        }
    }

    public long changeEnergy(long energyToAdd) {
        long energyAdded = 0L;
        for (IEnergyContainer container : this.energyContainers) {
            energyAdded += container.changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) {
                return energyAdded;
            }
        }
        return energyAdded;
    }

    public long removeEnergy(long energyToRemove) {
        return -changeEnergy(-energyToRemove);
    }

    public long getEnergyStored() {
        long energyStored = 0L;
        for (IEnergyContainer container : energyContainers) {
            energyStored += container.getEnergyStored();
        }
        return energyStored;
    }

    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        for (IEnergyContainer container : energyContainers) {
            energyCapacity += container.getEnergyCapacity();
        }
        return energyCapacity;
    }

    public long getInputPerSec() {
        long sum = 0;
        for (IEnergyContainer container : this.energyContainers) {
            sum += container.getInputPerSec();
        }
        return sum;
    }

    public long getOutputPerSec() {
        long sum = 0;
        for (IEnergyContainer container : this.energyContainers) {
            sum += container.getOutputPerSec();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "EnergyContainerList{" +
                "energyContainers=" + Arrays.toString(energyContainers) +
                ", totalEUt=" + totalEUt +
                ", effectiveVoltage=" + effectiveVoltage +
                ", highestVoltage=" + highestVoltage +
                ", tier=" + tier +
                ", energyStored=" + getEnergyStored() +
                ", energyCapacity=" + getEnergyCapacity() +
                '}';
    }
}
