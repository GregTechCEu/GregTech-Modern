package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class FusionReactorMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FusionReactorMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    public NotifiableEnergyContainer energyContainer;
    @Persisted(key = "energyStored")
    public long energySaveHack; // todo remove this, find actual fix (is this even needed? I'm not sure anymore...)
    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    @Persisted @DescSynced
    private long heat = 0;
    @Getter
    @DescSynced
    private Integer color = -1;

    public FusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.energyContainer = createEnergyContainer();
        this.energyContainer.addChangedListener(this::updateSaveHack);
    }

    public void updateSaveHack() {
        this.energySaveHack = energyContainer.getEnergyStored();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        return new NotifiableEnergyContainer(this, Integer.MAX_VALUE, 0, 0, 0, 0);
    }

    public void updateFormedValid() {
        if (!this.isFormed()) return;

        // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and has fully wiped recipe progress
        // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes it doubly hard to complete the recipe
        // (Will have to recover heat and recipe progress)
        if ((getRecipeLogic().isIdle() || !isWorkingEnabled() || (getRecipeLogic().isHasNotEnoughEnergy() && getRecipeLogic().progress == 0)) && heat > 0) {
            heat = heat <= 10000 ? 0 : (heat - 10000);
        }

        if (this.inputEnergyContainers != null) {
            if (this.inputEnergyContainers.getEnergyStored() > 0) {
                long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
                if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
            }
        }

        if (recipeLogic.isWorking() && color == -1) {
            if (recipeLogic.getLastRecipe() != null && recipeLogic.getLastRecipe().getOutputContents(FluidRecipeCapability.CAP).size() > 0) {
                int newColor = 0xFF000000 | getFluidColor(FluidRecipeCapability.CAP.of(recipeLogic.getLastRecipe().getOutputContents(FluidRecipeCapability.CAP).get(0).getContent()).getFluid().defaultFluidState());
                if (!Objects.equals(color, newColor)) {
                    color = newColor;
                }
            }
        } else if (!recipeLogic.isWorking() && isFormed() && color != -1) {
            color = -1;
        }
    }

    protected void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IRecipeHandler<?>> capabilities = this.getCapabilitiesProxy().get(IO.IN, EURecipeCapability.CAP);
        if (capabilities != null) {
            for (IRecipeHandler<?> handler : capabilities) {
                if (handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }
        } else {
            capabilities = this.getCapabilitiesProxy().get(IO.BOTH, EURecipeCapability.CAP);
            if (capabilities != null) {
                for (IRecipeHandler<?> handler : capabilities) {
                    if (handler instanceof IEnergyContainer container) {
                        energyContainers.add(container);
                    }
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        long euCapacity = calculateEnergyStorageFactor(tier, energyContainers.size());
        this.traits.remove(this.energyContainer);
        this.energyContainer = new NotifiableEnergyContainer(this, euCapacity, GTValues.V[tier], 0, 0, 0);
        this.energyContainer.addChangedListener(this::updateSaveHack);
    }

    public static long calculateEnergyStorageFactor(int tier, int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2, tier - LuV) * 10000000L;
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
    public void onLoad() {
        super.onLoad();
        this.energyContainer.setEnergyStored(energySaveHack);
        this.subscribeServerTick(this::updateFormedValid);
    }

    @Override
    public void onStructureFormed() {
        long energyStored = this.energyContainer.getEnergyStored();
        super.onStructureFormed();
        this.initializeAbilities();
        this.energyContainer.setEnergyStored(energyStored);
    }

    @ExpectPlatform
    public static int getFluidColor(FluidState fluid) {
        throw new AssertionError();
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
            return applyOC(recipe);
        }

        // if the remaining energy needed is more than stored, do not run
        if (energyContainer.getEnergyStored() < heatDiff)
            return null;

        // remove the energy needed
        energyContainer.removeEnergy(heatDiff);
        // increase the stored heat
        heat += heatDiff;
        return applyOC(recipe);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy", this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity()));
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", heat));
        }
    }

    public GTRecipe applyOC(GTRecipe recipe) {
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
}
