package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class DataBankMachine extends MultiblockControllerMachine implements IControllable {

    private IEnergyContainer energyContainer;

    @Getter @Setter
    @Persisted
    private boolean isActive = false;
    @Getter @Setter
    @Persisted
    private boolean isWorkingEnabled = true;
    protected boolean hasNotEnoughEnergy;

    @Getter
    private int energyUsage = 0;

    public DataBankMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Set<IOpticalDataAccessHatch> opticalReceptors = new HashSet<>();
        Set<IOpticalDataAccessHatch> opticalTransmitters = new HashSet<>();
        Set<IDataAccessHatch> regulars = new HashSet<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.BOTH ? handler.getHandlerIO() : io;
                if (handlerIO == IO.IN && handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                } else if (handlerIO == IO.IN && handler instanceof IOpticalDataAccessHatch hatch) {
                    opticalReceptors.add(hatch);
                } else if (handlerIO == IO.OUT && handler instanceof IOpticalDataAccessHatch hatch) {
                    opticalTransmitters.add(hatch);
                } else if (handlerIO == IO.IN && handler instanceof IDataAccessHatch hatch) {
                    regulars.add(hatch);
                }
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.energyUsage = calculateEnergyUsage(opticalReceptors, opticalTransmitters, regulars);
    }

    protected int calculateEnergyUsage(Set<IOpticalDataAccessHatch> opticalReceptors, Set<IOpticalDataAccessHatch> opticalTransmitters, Set<IDataAccessHatch> regularHatches) {
        int receivers = opticalReceptors.size();
        int transmitters = opticalTransmitters.size();
        int regulars = regularHatches.size();

        int dataHatches = receivers + transmitters + regulars;
        int tier = receivers > 0 ? GTValues.LuV : GTValues.EV;
        return GTValues.VA[tier] * dataHatches;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.energyUsage = 0;
    }

    protected void tickPower() {
        int energyToConsume = this.getEnergyUsage();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance;
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            energyToConsume += getTraits().stream().filter(IMaintenanceMachine.class::isInstance).map(IMaintenanceMachine.class::cast).findAny().get().getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    setActive(true);
                } else {
                    this.hasNotEnoughEnergy = true;
                    setActive(false);
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            setActive(false);
        }
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XDDDX", "XDDDX", "XDDDX")
                .aisle("XDDDX", "XAAAX", "XDDDX")
                .aisle("XCCCX", "XCSCX", "XCCCX")
                .where('S', selfPredicate())
                .where('X', states(getOuterState()))
                .where('D', states(getInnerState()).setMinGlobalLimited(3)
                        .or(abilities(MultiblockAbility.DATA_ACCESS_HATCH).setPreviewCount(3))
                        .or(abilities(MultiblockAbility.OPTICAL_DATA_TRANSMISSION)
                                .setMinGlobalLimited(1, 1))
                        .or(abilities(MultiblockAbility.OPTICAL_DATA_RECEPTION).setPreviewCount(1)))
                .where('A', states(getInnerState()))
                .where('C', states(getFrontState())
                        .setMinGlobalLimited(4)
                        .or(autoAbilities())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1)))
                .build();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.3"));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.4", GTValues.VA[GTValues.EV]));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.5", GTValues.VA[GTValues.LuV]));
    }
}
