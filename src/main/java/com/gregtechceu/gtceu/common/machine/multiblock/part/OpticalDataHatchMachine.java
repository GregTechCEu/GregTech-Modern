package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import com.gregtechceu.gtceu.api.capability.data.IStandardDataAccess;
import com.gregtechceu.gtceu.api.capability.data.query.DataQueryObject;
import com.gregtechceu.gtceu.api.capability.data.query.IBridgeable;
import com.gregtechceu.gtceu.api.capability.data.query.RecipeDataQuery;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpticalDataHatchMachine extends MultiblockPartMachine implements IStandardDataAccess {

    private final Set<DataQueryObject> recentQueries = GTUtil.createWeakHashSet();

    @Getter
    private final boolean isTransmitter;

    public OpticalDataHatchMachine(IMachineBlockEntity holder, boolean isTransmitter) {
        super(holder);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        if (!supportsQuery(queryObject) || !recentQueries.add(queryObject)) return false;
        if (!getControllers().isEmpty()) {
            if (isTransmitter()) {
                IMultiController controller = getControllers().get(0);
                if (!controller.isFormed() || (controller instanceof IWorkable workable && !workable.isActive()))
                    return false;

                List<IDataAccess> dataAccesses = new ArrayList<>();
                List<IStandardDataAccess> reception = new ArrayList<>();
                for (var part : controller.getParts()) {
                    Block block = part.self().getBlockState().getBlock();
                    if (part instanceof IDataAccess hatch && PartAbility.DATA_ACCESS.isApplicable(block)) {
                        dataAccesses.add(hatch);
                    }
                    if (part instanceof IStandardDataAccess hatch &&
                            PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                        reception.add(hatch);
                    }
                }

                if (IDataAccess.accessData(dataAccesses, queryObject))
                    return true;

                if (queryObject instanceof IBridgeable bridgeable && reception.size() > 1) {
                    bridgeable.setBridged();
                }
                return IDataAccess.accessData(reception, queryObject);
            } else {
                BlockEntity tileEntity = getNeighbor(getFrontFacing());
                if (tileEntity == null) return false;
                IDataAccess cap = tileEntity.getCapability(GTCapability.CAPABILITY_DATA_ACCESS,
                        getFrontFacing().getOpposite()).resolve().orElse(null);
                return cap != null && cap.accessData(queryObject);
            }
        }
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
        // creative hatches do not need to check, they always have the recipe
        RecipeDataQuery query = new RecipeDataQuery(recipe);

        // hatches need to have the recipe available
        if (this.accessData(query)) return recipe;
        return null;
    }
}
