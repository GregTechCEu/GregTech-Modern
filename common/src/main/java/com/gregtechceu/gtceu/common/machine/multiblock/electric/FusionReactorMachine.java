package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.multi.NotifiableMultiTransferItemStackHandler;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class FusionReactorMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FusionReactorMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);


    @Persisted
    public NotifiableEnergyContainer energyContainer;
    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    @Persisted
    @DescSynced
    private long heat = 0;
    @Getter
    @DescSynced
    private Integer color;

    public FusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.energyContainer = createEnergyContainer();
        this.initializeAbilities();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        return new NotifiableEnergyContainer(this, Integer.MAX_VALUE, 0, 0, 0, 0);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new FusionRecipeLogic(this);
    }

    protected void initializeAbilities() {
        if (this.getCapabilitiesProxy().contains(IO.IN, ItemRecipeCapability.CAP)) {
            List<IItemTransfer> itemsIn = this.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).stream().filter(IItemTransfer.class::isInstance).map(IItemTransfer.class::cast).toList();
            this.getCapabilitiesProxy().put(IO.IN, ItemRecipeCapability.CAP, List.of(new NotifiableMultiTransferItemStackHandler(this, itemsIn, IO.IN)));
        }
        if (this.getCapabilitiesProxy().contains(IO.IN, FluidRecipeCapability.CAP)) {
            List<FluidStorage> fluidsIn = this.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP).stream().filter(FluidStorage.class::isInstance).map(FluidStorage.class::cast).toList();
            this.getCapabilitiesProxy().put(IO.IN, FluidRecipeCapability.CAP, List.of(new NotifiableFluidTank(this, fluidsIn, IO.IN)));
        }
        if (this.getCapabilitiesProxy().contains(IO.OUT, ItemRecipeCapability.CAP)) {
            List<IItemTransfer> itemsOut = this.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).stream().filter(IItemTransfer.class::isInstance).map(IItemTransfer.class::cast).toList();
            this.getCapabilitiesProxy().put(IO.OUT, ItemRecipeCapability.CAP, List.of(new NotifiableMultiTransferItemStackHandler(this, itemsOut, IO.OUT)));
        }
        if (this.getCapabilitiesProxy().contains(IO.OUT, FluidRecipeCapability.CAP)) {
            List<FluidStorage> fluidsOut = this.getCapabilitiesProxy().get(IO.OUT, FluidRecipeCapability.CAP).stream().filter(FluidStorage.class::isInstance).map(FluidStorage.class::cast).toList();
            this.getCapabilitiesProxy().put(IO.OUT, FluidRecipeCapability.CAP, List.of(new NotifiableFluidTank(this, fluidsOut, IO.OUT)));
        }

        List<IEnergyContainer> energyInputs = this.getCapabilitiesProxy().get(IO.IN, EURecipeCapability.CAP).stream().filter(IEnergyContainer.class::isInstance).map(IEnergyContainer.class::cast).toList();
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = calculateEnergyStorageFactor(tier, energyInputs.size());
        this.energyContainer = new NotifiableEnergyContainer(this, euCapacity, GTValues.V[tier], 0, 0, 0);
    }

    public static long calculateEnergyStorageFactor(int tier, int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2, tier - 6) * 10000000L;
    }

    public static Block getCasingState(int tier) {
        return switch (tier) {
            case LuV -> FUSION_CASING.get();
            case ZPM -> FUSION_CASING_MK2.get();
            default -> FUSION_CASING_MK3.get();
        };
    }

    public static Block getCoilState(int tier) {
        if (tier == GTValues.LuV)
            return FUSION_CASING_SUPERCONDUCTOR.get();

        return FUSION_CASING_FUSION_COIL.get();
    }

    public static IFusionCasingType getCasingType(int tier) {
        return switch (tier) {
            case LuV -> FusionCasingBlock.CasingType.FUSION_CASING;
            case ZPM -> FusionCasingBlock.CasingType.FUSION_CASING_MK2;
            case UV -> FusionCasingBlock.CasingType.FUSION_CASING_MK3;
            default -> FusionCasingBlock.CasingType.FUSION_CASING;
        };
    }

    @Override
    public void onStructureFormed() {
        long energyStored = this.energyContainer.getEnergyStored();
        this.initializeAbilities();
        this.energyContainer.setEnergyStored(energyStored);
        if (this.inputEnergyContainers.getEnergyStored() > 0) {
            long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
            if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
        }
        super.onStructureFormed();
        if (recipeLogic.isWorking() && color == null) {
            if (recipeLogic.getLastRecipe() != null && recipeLogic.getLastRecipe().getOutputContents(FluidRecipeCapability.CAP).size() > 0) {
                int newColor = 0xFF000000 | getFluidColor(FluidRecipeCapability.CAP.of(recipeLogic.getLastRecipe().getOutputContents(FluidRecipeCapability.CAP).get(0).getContent()).getFluid().defaultFluidState());
                if (!Objects.equals(color, newColor)) {
                    color = newColor;
                }
            }
        } else if (!recipeLogic.isWorking() && isFormed() && color != null) {
            color = null;
        }
    }

    @ExpectPlatform
    public static int getFluidColor(FluidState fluid) {
        throw new AssertionError();
    }

    private class FusionRecipeLogic extends RecipeLogic {

        public FusionRecipeLogic(FusionReactorMachine tileEntity) {
            super(tileEntity);
        }

        @Override
        public void handleRecipeWorking() {
            super.handleRecipeWorking();
            // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and has fully wiped recipe progress
            // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes it doubly hard to complete the recipe
            // (Will have to recover heat and recipe progress)
            if ((!(isActive() || isWorkingEnabled()) || (isSuspend() && progress == 0)) && heat > 0) {
                heat = heat <= 10000 ? 0 : (heat - 10000);
            }
        }
    }

    @Override
    public long getMaxVoltage() {
        return Math.min(GTValues.V[tier], super.getMaxVoltage());
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        if (!recipe.data.contains("eu_to_start") || recipe.data.getLong("eu_to_start") > energyContainer.getEnergyCapacity()) {
            return null;
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
            return null;
        }

        long heatDiff = recipe.data.getLong("eu_to_start") - heat;
        // if the stored heat is >= required energy, recipe is okay to run
        if (heatDiff <= 0) {
            return RecipeHelper.applyOverclock(new OverclockingLogic(false) {
                @Override
                protected double getOverclockingDurationDivisor() {
                    return 2.0D;
                }

                @Override
                protected double getOverclockingVoltageMultiplier() {
                    return 2.0D;
                }
            }, recipe, getMaxVoltage());
        }

        // if the remaining energy needed is more than stored, do not run
        if (energyContainer.getEnergyStored() < heatDiff)
            return null;

        // remove the energy needed
        energyContainer.removeEnergy(heatDiff);
        // increase the stored heat
        heat += heatDiff;
        return recipe;
    }
}
