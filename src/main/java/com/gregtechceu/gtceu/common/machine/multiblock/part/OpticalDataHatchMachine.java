package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpticalDataHatchMachine extends MultiblockPartMachine implements IOpticalDataAccessHatch {

    @Getter
    private final boolean isTransmitter;

    public OpticalDataHatchMachine(IMachineBlockEntity holder, boolean isTransmitter) {
        super(holder);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        if (!getControllers().isEmpty()) {
            if (isTransmitter()) {
                IMultiController controller = getControllers().get(0);
                if (!(controller instanceof IWorkable workable) || !workable.isActive()) return false;

                List<IDataAccessHatch> data_accesses = new ArrayList<>();
                List<IDataAccessHatch> transmitters = new ArrayList<>();
                for (var part : controller.getParts()) {
                    Block block = part.self().getBlockState().getBlock();
                    if (part instanceof IDataAccessHatch hatch && PartAbility.DATA_ACCESS.isApplicable(block)) {
                        data_accesses.add(hatch);
                    }
                    if (part instanceof IDataAccessHatch hatch && PartAbility.COMPUTATION_DATA_TRANSMISSION.isApplicable(block)) {
                        transmitters.add(hatch);
                    }
                }

                return isRecipeAvailable(data_accesses, seen, recipe) ||
                    isRecipeAvailable(transmitters, seen,
                        recipe);
            } else {
                BlockEntity tileEntity = getLevel().getBlockEntity(getPos().relative(getFrontFacing()));
                if (tileEntity == null) return false;

                if (tileEntity instanceof OpticalPipeBlockEntity) {
                    //noinspection DataFlowIssue
                    IDataAccessHatch cap = tileEntity.getCapability(GTCapability.CAPABILITY_DATA_ACCESS,
                        getFrontFacing().getOpposite()).orElse(null);
                    //noinspection ConstantValue
                    return cap != null && cap.isRecipeAvailable(recipe, seen);
                }
            }
        }
        return false;
    }

    private static boolean isRecipeAvailable(@NotNull Iterable<? extends IDataAccessHatch> hatches,
                                             @NotNull Collection<IDataAccessHatch> seen,
                                             @NotNull GTRecipe recipe) {
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

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IOpticalDataAccessHatch.super.modifyRecipe(recipe);
    }
}
