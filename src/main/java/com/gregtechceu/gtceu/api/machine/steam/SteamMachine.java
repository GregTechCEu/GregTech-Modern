package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SteamMachine extends MetaMachine implements ITieredMachine {

    public static final BooleanProperty STEEL_PROPERTY = GTMachineModelProperties.IS_STEEL_MACHINE;

    @Getter
    public final boolean isHighPressure;
    @SaveField
    public final NotifiableFluidTank steamTank;

    public static abstract class SteamMachineTraits extends MetaMachineTraits {

        public NotifiableFluidTank steamTank(SteamMachine machine) {
            NotifiableFluidTank tank = new NotifiableFluidTank(machine, 1, 16 * FluidType.BUCKET_VOLUME, IO.IN);
            tank.setFilter(f -> f.getFluid().is(GTMaterials.Steam.getFluidTag()));
            return tank;
        }
    }

    public SteamMachine(IMachineBlockEntity holder, boolean isHighPressure, SteamMachineTraits traits) {
        super(holder);
        this.isHighPressure = isHighPressure;
        this.steamTank = traits.steamTank(this);
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    @Override
    public int getTier() {
        return isHighPressure ? 1 : 0;
    }
}
