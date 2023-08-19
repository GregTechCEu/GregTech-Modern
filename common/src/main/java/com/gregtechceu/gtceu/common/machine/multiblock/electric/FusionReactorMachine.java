package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    protected EnergyContainerList inputEnergyContainers;
    @Persisted
    protected long heat = 0;
    @Persisted
    protected final NotifiableEnergyContainer energyContainer;
    //TODO implement it when we do fancy effect again.
//    @Getter
//    @DescSynced
//    private Integer color = -1;
    @Nullable
    protected TickableSubscription preHeatSubs;

    public FusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.energyContainer = createEnergyContainer();
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableEnergyContainer createEnergyContainer() {
        // create an internal energy container for temp storage. its capacity is decided when the structure formed.
        // it doesn't provide any capability of all sides, but null for the goggles mod to check it storages.
        var container = new NotifiableEnergyContainer(this, 0, 0, 0, 0, 0);
        container.setCapabilityValidator(Objects::isNull);
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updatePreHeatSubscription();
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE || io == IO.OUT) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                    traitSubscriptions.add(handler.addChangedListener(this::updatePreHeatSubscription));
                }
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        energyContainer.resetBasicInfo(calculateEnergyStorageFactor(getTier(), energyContainers.size()), 0, 0, 0, 0);
        updatePreHeatSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        heat = 0;
        energyContainer.resetBasicInfo(0, 0, 0, 0, 0);
        energyContainer.setEnergyStored(0);
        updatePreHeatSubscription();
    }

    //////////////////////////////////////
    //*****      Recipe Logic     ******//
    //////////////////////////////////////
    protected void updatePreHeatSubscription() {
        // do preheat logic for heat cool down and charge internal energy container
        if (heat > 0 || (inputEnergyContainers != null && inputEnergyContainers.getEnergyStored() > 0 && energyContainer.getEnergyStored() < energyContainer.getEnergyCapacity())) {
            preHeatSubs = subscribeServerTick(preHeatSubs, this::updateHeat);
        } else if (preHeatSubs != null) {
            preHeatSubs.unsubscribe();
            preHeatSubs = null;
        }
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof FusionReactorMachine fusionReactorMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > fusionReactorMachine.getTier() ||
                    !recipe.data.contains("eu_to_start") ||
                    recipe.data.getLong("eu_to_start") > fusionReactorMachine.energyContainer.getEnergyCapacity()) {
                return null;
            }

            long heatDiff = recipe.data.getLong("eu_to_start") - fusionReactorMachine.heat;

            // if the stored heat is >= required energy, recipe is okay to run
            if (heatDiff <= 0) {
                return RecipeHelper.applyOverclock(new OverclockingLogic(2, 2), recipe, fusionReactorMachine.getMaxVoltage());
            }
            // if the remaining energy needed is more than stored, do not run
            if (fusionReactorMachine.energyContainer.getEnergyStored() < heatDiff)
                return null;

            // remove the energy needed
            fusionReactorMachine.energyContainer.removeEnergy(heatDiff);
            // increase the stored heat
            fusionReactorMachine.heat += heatDiff;
            fusionReactorMachine.updatePreHeatSubscription();
            return RecipeHelper.applyOverclock(new OverclockingLogic(2, 2), recipe, fusionReactorMachine.getMaxVoltage());
        }
        return null;
    }

    public void updateHeat() {
        // Drain heat when the reactor is not active, is paused via soft mallet, or does not have enough energy and has fully wiped recipe progress
        // Don't drain heat when there is not enough energy and there is still some recipe progress, as that makes it doubly hard to complete the recipe
        // (Will have to recover heat and recipe progress)
        if ((getRecipeLogic().isIdle() || !isWorkingEnabled() || (getRecipeLogic().isWaiting() && getRecipeLogic().getProgress() == 0)) && heat > 0) {
            heat = heat <= 10000 ? 0 : (heat - 10000);
        }
        // charge the internal energy storage
        var leftStorage = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
        if (inputEnergyContainers != null && leftStorage > 0) {
            energyContainer.addEnergy(inputEnergyContainers.removeEnergy(leftStorage));
        }
        updatePreHeatSubscription();
    }

//    @Override
//    public void onWorking() {
//        super.onWorking();
//        if (color == -1) {
//            var lastRecipe = recipeLogic.getLastRecipe();
//            if (lastRecipe != null && !lastRecipe.getOutputContents(FluidRecipeCapability.CAP).isEmpty()) {
//                int newColor = 0xFF000000 | FluidHelper.getColor(FluidRecipeCapability.CAP.of(lastRecipe.getOutputContents(FluidRecipeCapability.CAP).get(0).getContent()));
//                if (!Objects.equals(color, newColor)) {
//                    color = newColor;
//                }
//            }
//        }
//    }

//    @Override
//    public void onWaiting() {
//        super.onWaiting();
//        color = -1;
//    }

    @Override
    public void afterWorking() {
        super.afterWorking();
//        color = -1;
    }

    @Override
    public long getMaxVoltage() {
        return Math.min(GTValues.V[tier], super.getMaxVoltage());
    }

    //////////////////////////////////////
    //********       GUI       *********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy", this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity()));
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
            return SUPERCONDUCTING_COIL.get();

        return FUSION_COIL.get();
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
