package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineHatchMultiblock;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MachineHatchPartMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/7/23
 * @implNote ProcessingArrayMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingArrayMachine extends TieredWorkableElectricMultiblockMachine implements IMachineHatchMultiblock {
    //runtime
    @Nullable
    private GTRecipeType[] recipeTypeCache;

    public ProcessingArrayMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Nullable
    public MachineDefinition getMachineDefinition() {
        MachineHatchPartMachine part = getParts().stream().filter(MachineHatchPartMachine.class::isInstance).map(MachineHatchPartMachine.class::cast).findAny().orElse(null);
        if (part != null && part.getMachineStorage().getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
            return metaMachineItem.getDefinition();
        }
        return null;
    }

    @Override
    @Nonnull
    public GTRecipeType[] getRecipeTypes() {
        if (recipeTypeCache == null) {
            var definition = getMachineDefinition();
            recipeTypeCache = definition == null ? null : definition.getRecipeTypes();
        }
        if (recipeTypeCache == null) {
            recipeTypeCache = new GTRecipeType[]{GTRecipeTypes.DUMMY_RECIPES};
        }
        return recipeTypeCache;
    }

    @NotNull
    @Override
    public GTRecipeType getRecipeType() {
        return getRecipeTypes()[getActiveRecipeType()];
    }

    public void notifyMachineChanged() {
        recipeTypeCache = null;
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
        }
    }

    //////////////////////////////////////
    //*******    Recipe Logic    *******//
    //////////////////////////////////////

    /**
     * For available recipe tier, decided by the held machine.
     */
    @Override
    public int getTier() {
        var definition = getMachineDefinition();
        return definition == null ? 0 : definition.getTier();
    }

    @Override
    public int getOverclockTier() {
        MachineDefinition machineDefinition = getMachineDefinition();
        int machineTier = machineDefinition == null ? getDefinition().getTier() : Math.min(getDefinition().getTier(), machineDefinition.getTier());
        return Math.min(machineTier, GTUtil.getTierByVoltage(getMaxVoltage()));
    }

    @Override
    public int getMinOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public long getMaxVoltage() {
        return getMaxHatchVoltage();
    }

    @Override
    public int getMachineLimit() {
        return getMachineLimit(getDefinition().getTier());
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof WorkableElectricMultiblockMachine processingArray) {
            MachineHatchPartMachine machineHatch = processingArray.getParts().stream().filter(MachineHatchPartMachine.class::isInstance).map(MachineHatchPartMachine.class::cast).findAny().orElse(null);
            if (machineHatch == null || machineHatch.getMachineStorage().getStackInSlot(0).isEmpty()) {
                return null;
            }

            if (RecipeHelper.getRecipeEUtTier(recipe) > processingArray.getTier())
                return null;

            int parallelLimit = Math.min(
                machineHatch.getMachineStorage().storage.getStackInSlot(0).getCount(),
                (int) (processingArray.getMaxVoltage() / RecipeHelper.getInputEUt(recipe))
            );

            if (parallelLimit <= 0)
                return null;

            // apply parallel first
            var parallel = Objects.requireNonNull(GTRecipeModifiers.accurateParallel(
                machine, recipe, Math.min(parallelLimit, getMachineLimit(machine.getDefinition().getTier())), false
            ));
            int parallelCount = parallel.getB();
            recipe = parallel.getA();

            // apply overclock afterward
            long maxVoltage = Math.min(processingArray.getOverclockVoltage() * parallelCount, processingArray.getMaxVoltage());
            recipe = RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK, recipe, maxVoltage);

            return recipe;
        }
        return null;
    }

    @Override
    public Map<RecipeCapability<?>, Integer> getOutputLimits() {
        if (getMachineDefinition() != null) {
            return getMachineDefinition().getRecipeOutputLimits();
        }
        return GTRegistries.RECIPE_CAPABILITIES.values().stream().map(key -> Map.entry(key, 0)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //////////////////////////////////////
    //********        Gui       ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isActive()) {
            textList.add(Component.translatable("gtceu.machine.machine_hatch.locked").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
    }

    //////////////////////////////////////
    //********     Structure    ********//
    //////////////////////////////////////
    public static Block getCasingState(int tier) {
        if (tier <= GTValues.IV) {
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        } else {
            return GTBlocks.CASING_HSSE_STURDY.get();
        }
    }

    public static int getMachineLimit(int tier) {
        return tier <= GTValues.IV ? 16 : 64;
    }
}
