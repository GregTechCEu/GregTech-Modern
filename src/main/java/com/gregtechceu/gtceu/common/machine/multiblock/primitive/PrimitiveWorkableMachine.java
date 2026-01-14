package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardEmitter;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveWorkableMachine extends WorkableMultiblockMachine
                                      implements IMachineLife, IEnvironmentalHazardEmitter {

    @SaveField
    public final NotifiableItemStackHandler importItems;
    @SaveField
    public final NotifiableItemStackHandler exportItems;
    @SaveField
    public final NotifiableFluidTank importFluids;
    @SaveField
    public final NotifiableFluidTank exportFluids;

    public PrimitiveWorkableMachine(BlockEntityCreationInfo info) {
        super(info);
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableItemStackHandler createImportItemHandler() {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler() {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    protected NotifiableFluidTank createImportFluidHandler() {
        return new NotifiableFluidTank(this, getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                32 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    protected NotifiableFluidTank createExportFluidHandler() {
        return new NotifiableFluidTank(this, getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP),
                32 * FluidType.BUCKET_VOLUME, IO.OUT);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(importItems.storage);
        clearInventory(exportItems.storage);
    }

    @Override
    public float getHazardStrengthPerOperation() {
        return 0.1f;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        spreadEnvironmentalHazard();
    }
}
