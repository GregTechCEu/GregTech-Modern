package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveWorkableMachine extends WorkableMultiblockMachine {

    @SaveField
    public final NotifiableItemStackHandler importItems;
    @SaveField
    public final NotifiableItemStackHandler exportItems;
    @SaveField
    public final NotifiableFluidTank importFluids;
    @SaveField
    public final NotifiableFluidTank exportFluids;

    @Getter
    private final EnvironmentalHazardEmitterTrait hazardEmitter;

    /**
     * Creates a {@link PrimitiveWorkableMachine}.
     *
     * @param info             {@link BlockEntityCreationInfo}
     * @param recipeLogic      The recipe logic to use.
     * @param importSlots      The amount of item input slots this machine should have (can be 0).
     * @param exportSlots      The amount of item output slots this machine should have (can be 0).
     * @param fluidImportSlots The amount of fluid input slots this machine should have (can be 0).
     * @param fluidExportSlots The amount of fluid output slots this machine should have (can be 0).
     * @param tankCapacity     The fluid tank capacity.
     */
    public PrimitiveWorkableMachine(BlockEntityCreationInfo info, RecipeLogic recipeLogic, int importSlots,
                                    int exportSlots,
                                    int fluidImportSlots, int fluidExportSlots,
                                    int tankCapacity) {
        super(info, recipeLogic);
        this.importItems = attachTrait(new NotifiableItemStackHandler(importSlots, IO.IN, IO.BOTH));
        this.exportItems = attachTrait(new NotifiableItemStackHandler(exportSlots, IO.OUT));
        this.importFluids = attachTrait(new NotifiableFluidTank(fluidImportSlots, tankCapacity, IO.IN, IO.BOTH));
        this.exportFluids = attachTrait(new NotifiableFluidTank(fluidExportSlots, tankCapacity, IO.OUT));
        this.hazardEmitter = attachTrait(
                new EnvironmentalHazardEmitterTrait(GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                        0.1f));
    }

    /**
     * Creates a {@link WorkableTieredMachine} with default settings.<br>
     * The amount of item and fluid input and output slots is determined by the machine's recipe type.
     *
     * @param info {@link BlockEntityCreationInfo}
     */
    public PrimitiveWorkableMachine(BlockEntityCreationInfo info) {
        super(info);
        this.importItems = attachTrait(
                new NotifiableItemStackHandler(getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN));
        this.exportItems = attachTrait(
                new NotifiableItemStackHandler(
                        getDefinition().getOutputSize(ItemRecipeCapability.CAP, getRecipeTypes()), IO.OUT));
        this.importFluids = attachTrait(new NotifiableFluidTank(getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                32 * FluidType.BUCKET_VOLUME, IO.IN));
        this.exportFluids = attachTrait(
                new NotifiableFluidTank(getDefinition().getOutputSize(FluidRecipeCapability.CAP, getRecipeTypes()),
                        32 * FluidType.BUCKET_VOLUME, IO.OUT));
        this.hazardEmitter = attachTrait(
                new EnvironmentalHazardEmitterTrait(GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                        0.1f));
    }
}
