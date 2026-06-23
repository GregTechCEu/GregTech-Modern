package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResearchStationMachine extends WorkableElectricMultiblockMachine
                                    implements IOpticalComputationReceiver {

    @Getter
    private IOpticalComputationProvider computationProvider;
    @Getter
    private ObjectHolderMachine objectHolder;

    public ResearchStationMachine(BlockEntityCreationInfo info) {
        super(info, new ResearchStationRecipeLogic());
    }

    @Override
    public ResearchStationRecipeLogic getRecipeLogic() {
        return (ResearchStationRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        var pState = patternStates.get(substructureName);
        super.formStructure(substructureName);
        for (IMultiPart part : getParts()) {
            if (part instanceof ObjectHolderMachine holder) {
                if (holder.getFrontFacing() != getFrontFacing().getOpposite()) {
                    pState.setError(new PatternStringError(
                            Component.translatable("gtceu.predicate_error.object_holder.direction")));
                    invalidateStructure(substructureName);
                    return;
                }
                this.objectHolder = holder;
            }

            part.self()
                    .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER)
                    .ifPresent(provider -> this.computationProvider = provider);
        }

        // should never happen, but would rather do this than have an obscure NPE
        if (computationProvider == null) {
            pState.setError(new PatternStringError(
                    Component.translatable("gtceu.predicate_error.research.missing_computation")));
            invalidateStructure(substructureName);
        }

        if (objectHolder == null) {
            pState.setError(new PatternStringError(
                    Component.translatable("gtceu.predicate_error.research.missing_object_holder")));
            invalidateStructure(substructureName);
        }
    }

    @Override
    public PatternState checkStructurePattern(String name) {
        var patternState = super.checkStructurePattern(name);
        // var cache = getSubstructure(name).getCache();
        var cache = patternStates.get(name).getCache();
        ObjectHolderMachine objHolder = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            if (entry.getValue().getBlockEntity() instanceof ObjectHolderMachine objectHolder) {
                objHolder = objectHolder;
            }
        }
        if (objHolder != null && objHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
            patternState.setError(
                    new PatternStringError(Component.translatable("gtceu.predicate_error.object_holder.direction")));
            invalidateStructure(name);
        }
        return patternState;
    }

    @Override
    public void invalidateStructure(String name) {
        computationProvider = null;
        // recheck the ability to make sure it wasn't the one broken
        for (IMultiPart part : getParts()) {
            if (part instanceof ObjectHolderMachine holder) {
                if (holder == objectHolder) {
                    objectHolder.setLocked(false);
                }
            }
        }
        objectHolder = null;
        super.invalidateStructure(name);
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager,
                () -> Component.translatable("gtceu.multiblock.research_station.researching")
                        .withStyle(ChatFormatting.GREEN)));
        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyUsageLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        // .addComputationUsageExactLine(computationProvider.getMaxCWUt()) // TODO: (Onion)
        widgets.add(GTMultiblockTextUtil.addProgressLinePercentOnly(this, syncManager));
        return widgets;
    }

    public static class ResearchStationRecipeLogic extends RecipeLogic {

        public ResearchStationRecipeLogic() {
            super();
        }

        @Override
        public ResearchStationMachine getMachine() {
            return (ResearchStationMachine) super.getMachine();
        }

        @Override
        protected List<Class<?>> validMachineClasses() {
            return List.of(ResearchStationMachine.class);
        }

        // skip "can fit" checks, it can always fit
        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            var match = matchRecipeNoOutput(recipe);
            if (!match.isSuccess()) return match;

            return matchTickRecipeNoOutput(recipe);
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var modified = getMachine().fullModifyRecipe(match);
            if (modified != null) {
                // What is the point of this
                if (!modified.inputs.containsKey(CWURecipeCapability.CAP) &&
                        !modified.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                    return true;
                }
                var recipeMatch = checkRecipe(modified);
                if (recipeMatch.isSuccess()) {
                    setupRecipe(modified);
                } else {
                    setWaiting(recipeMatch.reason());
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastOriginRecipe = match;
                    lastFailedMatches = null;
                    return true;
                }
            }
            return false;
        }

        protected ActionResult matchRecipeNoOutput(GTRecipe recipe) {
            if (!getMachine().hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
            return RecipeHelper.handleRecipe(getMachine(), recipe, IO.IN, recipe.inputs, Collections.emptyMap(), false,
                    true);
        }

        protected ActionResult matchTickRecipeNoOutput(GTRecipe recipe) {
            if (recipe.hasTick()) {
                if (!getMachine().hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
                return RecipeHelper.handleRecipe(getMachine(), recipe, IO.IN, recipe.tickInputs, Collections.emptyMap(),
                        false, true);
            }
            return ActionResult.SUCCESS;
        }

        // Handle RecipeIO manually
        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                // lock the object holder on recipe start
                ObjectHolderMachine holder = getMachine().getObjectHolder();
                holder.setLocked(true);
                return ActionResult.SUCCESS;
            }

            // "replace" the items in the slots rather than outputting elsewhere
            // unlock the object holder
            ObjectHolderMachine holder = getMachine().getObjectHolder();
            if (lastRecipe == null) {
                holder.setLocked(false);
                return ActionResult.SUCCESS;
            }

            holder.setHeldItem(ItemStack.EMPTY);
            ItemStack outputItem = ItemStack.EMPTY;
            var contents = lastRecipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!contents.isEmpty()) {
                outputItem = ItemRecipeCapability.CAP.of(contents.get(0).content()).getItems()[0];
            }
            if (!outputItem.isEmpty()) {
                holder.setDataItem(outputItem.copy());
            }
            holder.setLocked(false);
            return ActionResult.SUCCESS;
        }

        @Override
        protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                return super.handleTickRecipeIO(recipe, io);
            }
            return ActionResult.SUCCESS;
        }
    }
}
