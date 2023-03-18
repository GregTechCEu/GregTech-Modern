package com.lowdragmc.gtceu.blockentity;

import com.lowdragmc.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.capability.IControllable;
import com.lowdragmc.gtceu.api.capability.IEnergyContainer;
import com.lowdragmc.gtceu.api.capability.IWorkable;
import com.lowdragmc.gtceu.api.capability.forge.GTCapabilities;
import com.lowdragmc.gtceu.api.machine.trait.MachineTrait;
import com.lowdragmc.gtceu.api.misc.EnergyContainerList;
import com.lowdragmc.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachineBlockEntity
 */
public class MetaMachineBlockEntityImpl extends MetaMachineBlockEntity {

    public MetaMachineBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapabilities.CAPABILITY_COVERABLE) {
            return GTCapabilities.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(() -> getMetaMachine().getCoverContainer()));
        } else if (cap == GTCapabilities.CAPABILITY_TOOLABLE) {
            return GTCapabilities.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(this::getMetaMachine));
        } else if (cap == GTCapabilities.CAPABILITY_WORKABLE) {
            if (getMetaMachine() instanceof IWorkable workable) {
                return GTCapabilities.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
            }
            for (MachineTrait trait : getMetaMachine().getTraits()) {
                if (trait instanceof IWorkable workable) {
                    return GTCapabilities.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
                }
            }
        } else if (cap == GTCapabilities.CAPABILITY_CONTROLLABLE) {
            if (getMetaMachine() instanceof IControllable controllable) {
                return GTCapabilities.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
            }
            for (MachineTrait trait : getMetaMachine().getTraits()) {
                if (trait instanceof IControllable controllable) {
                    return GTCapabilities.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
                }
            }
        } else if (cap == GTCapabilities.CAPABILITY_RECIPE_LOGIC) {
            for (MachineTrait trait : getMetaMachine().getTraits()) {
                if (trait instanceof RecipeLogic recipeLogic) {
                    return GTCapabilities.CAPABILITY_RECIPE_LOGIC.orEmpty(cap, LazyOptional.of(() -> recipeLogic));
                }
            }
        } else if (cap == GTCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (getMetaMachine() instanceof IEnergyContainer energyContainer) {
                return GTCapabilities.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = getMetaMachine().getTraits().stream().filter(IEnergyContainer.class::isInstance).filter(t -> t.hasCapability(side)).map(IEnergyContainer.class::cast).toList();
            if (!list.isEmpty()) {
                return GTCapabilities.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyContainerList(list)));
            }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            var transfer = getMetaMachine().getItemTransferCap(side);
            if (transfer != null) {
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(transfer)));
            }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var transfer = getMetaMachine().getFluidTransferCap(side);
            if (transfer != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> FluidTransferHelperImpl.toFluidHandler(transfer)));
            }
        }
        return super.getCapability(cap, side);
    }
}
