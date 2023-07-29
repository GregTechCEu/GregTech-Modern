package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import lombok.Getter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;

public class OpticalDataAccessHatchPartMachine extends TieredIOPartMachine implements IOpticalDataAccessHatch {

    @Getter
    private final boolean isTransmitter;


    public OpticalDataAccessHatchPartMachine(IMachineBlockEntity holder, boolean isTransmitter) {
        super(holder, GTValues.LuV, IO.IN);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        if (isFormed()) {
            if (isTransmitter()) {
                IMultiController controller = getControllers().get(0);

                return isRecipeAvailable(controller.getParts().stream().filter(IDataAccessHatch.class::isInstance).map(IDataAccessHatch.class::cast).toList(), seen, recipe) ||
                        isRecipeAvailable(controller.getParts().stream().filter(IOpticalDataAccessHatch.class::isInstance).map(IOpticalDataAccessHatch.class::cast).filter(hatch -> !hatch.isTransmitter()).toList(), seen, recipe);
            } else {
                BlockEntity tileEntity = getLevel().getBlockEntity(getPos().relative(getFrontFacing()));
                if (tileEntity == null) return false;

                if (tileEntity instanceof OpticalPipeBlockEntity) {
                    IDataAccessHatch cap = GTCapabilityHelper.getDataAccess(getLevel(), tileEntity.getBlockPos(), getFrontFacing().getOpposite());
                    return cap != null && cap.isRecipeAvailable(recipe, seen);
                }
            }
        }
        return false;
    }

    private static boolean isRecipeAvailable(@Nonnull Iterable<? extends IDataAccessHatch> hatches,
                                             @Nonnull Collection<IDataAccessHatch> seen,
                                             @Nonnull GTRecipe recipe) {
        for (IDataAccessHatch hatch : hatches) {
            if (seen.contains(hatch)) continue;
            if (hatch.isRecipeAvailable(recipe, seen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
