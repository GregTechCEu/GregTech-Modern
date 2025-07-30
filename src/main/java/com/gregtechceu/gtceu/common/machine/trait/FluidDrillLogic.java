package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class FluidDrillLogic extends RecipeLogic {

    public static final int MAX_PROGRESS = 20;

    @Getter
    @Nullable
    private Fluid veinFluid;

    public FluidDrillLogic(FluidDrillMachine machine) {
        super(machine);
    }

    @Override
    public FluidDrillMachine getMachine() {
        return (FluidDrillMachine) super.getMachine();
    }

    @Override
    public void findAndHandleRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            lastRecipe = null;
            var data = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
            if (veinFluid == null) {
                this.veinFluid = data.getFluidInChunk(getChunkX(), getChunkZ());
                if (this.veinFluid == null) {
                    if (subscription != null) {
                        subscription.unsubscribe();
                        subscription = null;
                    }
                    return;
                }
            }
            var match = getFluidDrillRecipe();
            if (match != null) {
                if (RecipeHelper.matchContents(this.machine, match).isSuccess()) {
                    setupRecipe(match);
                }
            }
        }
    }

    @Nullable
    private GTRecipe getFluidDrillRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel && veinFluid != null) {
            var data = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
            var recipe = GTRecipeBuilder.ofRaw()
                    .duration(MAX_PROGRESS)
                    .EUt(GTValues.VA[getMachine().getEnergyTier()])
                    .outputFluids(new FluidStack(veinFluid,
                            getFluidToProduce(data.getFluidVeinWorldEntry(getChunkX(), getChunkZ()))))
                    .buildRawRecipe();
            if (RecipeHelper.matchContents(getMachine(), recipe).isSuccess()) {
                return recipe;
            }
        }
        return null;
    }

    public int getFluidToProduce() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel && veinFluid != null) {
            var data = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
            return getFluidToProduce(data.getFluidVeinWorldEntry(getChunkX(), getChunkZ()));
        }
        return 0;
    }

    private int getFluidToProduce(FluidVeinWorldEntry entry) {
        var definition = entry.getDefinition();
        if (definition != null) {
            int depletedYield = definition.getDepletedYield();
            int regularYield = entry.getFluidYield();
            int remainingOperations = entry.getOperationsRemaining();

            int produced = Math.max(depletedYield,
                    regularYield * remainingOperations / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
            produced *= FluidDrillMachine.getRigMultiplier(getMachine().getTier());

            // Overclocks produce 50% more fluid
            if (isOverclocked()) {
                produced = produced * 3 / 2;
            }
            return produced;
        }
        return 0;
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            RecipeHelper.handleRecipeIO(this.machine, lastRecipe, IO.OUT, this.chanceCaches);
        }
        depleteVein();
        // try it again
        var match = getFluidDrillRecipe();
        if (match != null) {
            if (RecipeHelper.matchContents(this.machine, match).isSuccess()) {
                setupRecipe(match);
                return;
            }
        }
        if (suspendAfterFinish) {
            setStatus(Status.SUSPEND);
            suspendAfterFinish = false;
        } else {
            setStatus(Status.IDLE);
        }
        progress = 0;
        duration = 0;
    }

    protected void depleteVein() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            int chance = FluidDrillMachine.getDepletionChance(getMachine().getTier());
            var data = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
            // chance to deplete based on the rig
            if (chance == 1 || GTValues.RNG.nextInt(chance) == 0) {
                data.depleteVein(getChunkX(), getChunkZ(), 0, false);
            }
        }
    }

    protected boolean isOverclocked() {
        return getMachine().getEnergyTier() > getMachine().getTier();
    }

    private int getChunkX() {
        return SectionPos.blockToSectionCoord(getMachine().getPos().getX());
    }

    private int getChunkZ() {
        return SectionPos.blockToSectionCoord(getMachine().getPos().getZ());
    }
}
