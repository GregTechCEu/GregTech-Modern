package com.gregtechceu.gtceu.api.blockentity.forge;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.forge.GTEnergyHelperImpl;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.misc.LaserContainerList;
import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidEndpointMachine;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemEndpointMachine;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MetaMachineBlockEntityImpl(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var result = getCapability(getMetaMachine(), cap, side);
        return result == null ? super.getCapability(cap, side) : result;
    }

    @Nullable
    public static <T> LazyOptional<T> getCapability(MetaMachine machine,  @NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(machine::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> machine));
        } else if (cap == GTCapability.CAPABILITY_WORKABLE) {
            if (machine instanceof IWorkable workable) {
                return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
            }
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof IWorkable workable) {
                    return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_CONTROLLABLE) {
            if (machine instanceof IControllable controllable) {
                return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
            }
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof IControllable controllable) {
                    return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_RECIPE_LOGIC) {
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof RecipeLogic recipeLogic) {
                    return GTCapability.CAPABILITY_RECIPE_LOGIC.orEmpty(cap, LazyOptional.of(() -> recipeLogic));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            if (machine instanceof IEnergyContainer energyContainer) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = machine.getTraits().stream().filter(IEnergyContainer.class::isInstance).filter(t -> t.hasCapability(side)).map(IEnergyContainer.class::cast).toList();
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyContainerList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_CLEANROOM_RECEIVER) {
            if (machine instanceof ICleanroomReceiver cleanroomReceiver) {
                return GTCapability.CAPABILITY_CLEANROOM_RECEIVER.orEmpty(cap, LazyOptional.of(() -> cleanroomReceiver));
            }
        } else if (cap == GTCapability.CAPABILITY_MAINTENANCE_MACHINE ) {
            if (machine instanceof IMaintenanceMachine maintenanceMachine) {
                return GTCapability.CAPABILITY_MAINTENANCE_MACHINE.orEmpty(cap, LazyOptional.of(() -> maintenanceMachine));
            }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (machine instanceof LDItemEndpointMachine fluidEndpointMachine) {
                if (machine.getLevel().isClientSide) return null;
                ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                if (endpoint == null) return null;
                Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                IItemTransfer transfer = ItemTransferHelperImpl.getItemTransfer(machine.getLevel(), endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                if (transfer != null) {
                    return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() ->
                            ItemTransferHelperImpl.toItemHandler(new LDItemEndpointMachine.ItemHandlerWrapper(transfer))
                    ));
                }
            }
            var transfer = machine.getItemTransferCap(side);
            if (transfer != null) {
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(transfer)));
            }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (machine instanceof LDFluidEndpointMachine fluidEndpointMachine) {
                if (machine.getLevel().isClientSide) return null;
                ILDEndpoint endpoint = fluidEndpointMachine.getLink();
                if (endpoint == null) return null;
                Direction outputFacing = fluidEndpointMachine.getOutputFacing();
                IFluidTransfer transfer = FluidTransferHelper.getFluidTransfer(machine.getLevel(), endpoint.getPos().relative(outputFacing), outputFacing.getOpposite());
                if (transfer != null) {
                    return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() ->
                            FluidTransferHelperImpl.toFluidHandler(new LDFluidEndpointMachine.FluidHandlerWrapper(transfer))
                    ));
                }
            }
            var transfer = machine.getFluidTransferCap(side);
            if (transfer != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> FluidTransferHelperImpl.toFluidHandler(transfer)));
            }
        } else if (cap == ForgeCapabilities.ENERGY) {
            if (machine instanceof IPlatformEnergyStorage platformEnergyStorage) {
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> GTEnergyHelperImpl.toEnergyStorage(platformEnergyStorage)));
            }
            var list = machine.getTraits().stream().filter(IPlatformEnergyStorage.class::isInstance).filter(t -> t.hasCapability(side)).map(IPlatformEnergyStorage.class::cast).toList();
            if (!list.isEmpty()) {
                // TODO wrap list in the future
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> GTEnergyHelperImpl.toEnergyStorage(list.get(0))));
            }
        } else if (cap == GTCapability.CAPABILITY_LASER) {
            if (machine instanceof ILaserContainer energyContainer) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = machine.getTraits().stream().filter(ILaserContainer.class::isInstance).filter(t -> t.hasCapability(side)).map(ILaserContainer.class::cast).toList();
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new LaserContainerList(list)));
            }

        }
        return null;
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> metaMachineBlockEntityBlockEntityType) {
    }

    /**
     * Why, Forge, Why?
     * Why must you make me add a method for no good reason?
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getRenderBoundingBox() {
        GTRendererProvider instance = GTRendererProvider.getInstance();
        if (instance != null) {
            IRenderer renderer = instance.getRenderer(this);
            if (renderer != null) {
                if (renderer.getViewDistance() == 64 /*the default*/) {
                    return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
                }

                int viewDistHalf = renderer.getViewDistance() / 2;
                return new AABB(worldPosition).inflate(viewDistHalf);
            }
        }
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }
}
