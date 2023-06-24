package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactorMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FusionReactorMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final int tier;
    @Nullable
    private EnergyContainerList inputEnergyContainers;
    @Persisted @DescSynced
    private long heat = 0;
    @Getter
    @DescSynced
    private Integer color = -1;
    @Nullable
    protected TickableSubscription heatSubs;

    public FusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateHeatSubscription();
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
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
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        heat = 0;
        updateHeatSubscription();
    }

    //////////////////////////////////////
    //*****      Recipe Logic     ******//
    //////////////////////////////////////
    protected void updateHeatSubscription() {
        if (heat > 0) {
            heatSubs = subscribeServerTick(heatSubs, this::updateHeat);
        } else if (heatSubs != null) {
            heatSubs.unsubscribe();
            heatSubs = null;
        }
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier() ||
                !recipe.data.contains("eu_to_start") ||
                inputEnergyContainers == null ||
                recipe.data.getLong("eu_to_start") > inputEnergyContainers.getEnergyCapacity()) {
            return null;
        }

        long heatDiff = recipe.data.getLong("eu_to_start") - heat;

        // if the stored heat is >= required energy, recipe is okay to run
        if (heatDiff <= 0) {
            return applyOC(recipe);
        }
        // if the remaining energy needed is more than stored, do not run
        if (inputEnergyContainers.getEnergyStored() < heatDiff)
            return null;

        // remove the energy needed
        inputEnergyContainers.removeEnergy(heatDiff);
        // increase the stored heat
        heat += heatDiff;
        updateHeatSubscription();
        return applyOC(recipe);
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

    public void updateHeat() {
        // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and has fully wiped recipe progress
        // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes it doubly hard to complete the recipe
        // (Will have to recover heat and recipe progress)
        if ((getRecipeLogic().isIdle() || !isWorkingEnabled() || (getRecipeLogic().isHasNotEnoughEnergy() && getRecipeLogic().progress == 0)) && heat > 0) {
            heat = heat <= 10000 ? 0 : (heat - 10000);
        }
        updateHeatSubscription();
    }

    @Override
    public void onWorking() {
        if (color == -1) {
            var lastRecipe = recipeLogic.getLastRecipe();
            if (lastRecipe != null && lastRecipe.getOutputContents(FluidRecipeCapability.CAP).size() > 0) {
                int newColor = 0xFF000000 | FluidHelper.getColor(FluidRecipeCapability.CAP.of(lastRecipe.getOutputContents(FluidRecipeCapability.CAP).get(0).getContent()));
                if (!Objects.equals(color, newColor)) {
                    color = newColor;
                }
            }
        }
    }

    @Override
    public void onWaiting() {
        color = -1;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        color = -1;
    }

    //////////////////////////////////////
    //********       GUI       *********//
    //////////////////////////////////////
    @Override
    public long getMaxVoltage() {
        return Math.min(GTValues.V[tier], super.getMaxVoltage());
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed() && inputEnergyContainers != null) {
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy", this.inputEnergyContainers.getEnergyStored(), this.inputEnergyContainers.getEnergyCapacity()));
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", heat));
        }
    }

    //////////////////////////////////////
    //********      MISC       *********//
    //////////////////////////////////////
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

}
