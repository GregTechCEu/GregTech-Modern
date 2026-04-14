package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
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

    public PrimitiveWorkableMachine(BlockEntityCreationInfo info) {
        super(info);
        this.importItems = attachTrait(createImportItemHandler());
        this.exportItems = attachTrait(createExportItemHandler());
        this.importFluids = attachTrait(createImportFluidHandler());
        this.exportFluids = attachTrait(createExportFluidHandler());
        this.hazardEmitter = attachTrait(
                new EnvironmentalHazardEmitterTrait(GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                        0.1f));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableItemStackHandler createImportItemHandler() {
        return new NotifiableItemStackHandler(getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler() {
        return new NotifiableItemStackHandler(getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    protected NotifiableFluidTank createImportFluidHandler() {
        return new NotifiableFluidTank(getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                32 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    protected NotifiableFluidTank createExportFluidHandler() {
        return new NotifiableFluidTank(getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP),
                32 * FluidType.BUCKET_VOLUME, IO.OUT);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        importItems.dropInventoryInWorld();
        exportItems.dropInventoryInWorld();
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        hazardEmitter.emitHazard();
    }
}
