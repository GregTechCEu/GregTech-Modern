package com.gregtechceu.gtceu.api.blockentity.fabric;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.lowdragmc.lowdraglib.side.fluid.fabric.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.side.item.fabric.ItemTransferHelperImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    private static <T, R> Optional<R> castInstance(T object, Class<R> castTo) {
        if (castTo.isInstance(object.getClass())) {
            return Optional.of(castTo.cast(object));
        } else return Optional.empty();
    }

    private static <R> Optional<R> getMachineCapability(BlockEntity blockEntity, Function<MetaMachine, R> mapper) {
        IMachineBlockEntity machineBlockEntity = (IMachineBlockEntity) blockEntity;
        MetaMachine machine = machineBlockEntity.getMetaMachine();

        return Optional.ofNullable(mapper.apply(machine));
    }

    private static <T> Optional<T> getMachineCapability(BlockEntity blockEntity, Class<T> capabilityInterface) {
        IMachineBlockEntity machineBlockEntity = (IMachineBlockEntity) blockEntity;
        MetaMachine machine = machineBlockEntity.getMetaMachine();

        return castInstance(machine, capabilityInterface);
    }

    private static <T> Optional<T> getTraitCapability(BlockEntity blockEntity, Class<T> capabilityInterface) {
        IMachineBlockEntity machineBlockEntity = (IMachineBlockEntity) blockEntity;
        MetaMachine machine = machineBlockEntity.getMetaMachine();

        return machine.getTraits().stream()
                .map(t -> castInstance(t, capabilityInterface))
                .reduce(Optional.empty(), (acc, trait) -> acc.or(() -> trait));
    }

    private static <T, C extends T> Optional<T> getAllTraitCapabilities(
            BlockEntity blockEntity,
            Class<T> capabilityInterface,
            Function<List<T>, C> capabilityContainerFactory
    ) {
        IMachineBlockEntity machineBlockEntity = (IMachineBlockEntity) blockEntity;
        MetaMachine machine = machineBlockEntity.getMetaMachine();

        List<T> capabilities = machine.getTraits().stream()
                .map(t -> castInstance(t, capabilityInterface))
                .flatMap(Optional::stream)
                .toList();

        if (capabilities.size() == 0) return Optional.empty();
        if (capabilities.size() == 1) return Optional.ofNullable(capabilities.get(0));

        return Optional.ofNullable(capabilityContainerFactory.apply(capabilities));
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> type) {
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, MetaMachine::getCoverContainer)
                        .orElse(null),
                type
        );
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, IToolable.class::cast)
                        .orElse(null),
                type
        );
        GTCapability.CAPABILITY_WORKABLE.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, IWorkable.class)
                        .or(() -> getTraitCapability(blockEntity, IWorkable.class))
                        .orElse(null),
                type
        );
        GTCapability.CAPABILITY_CONTROLLABLE.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, IControllable.class)
                        .or(() -> getTraitCapability(blockEntity, IControllable.class))
                        .orElse(null),
                type
        );
        GTCapability.CAPABILITY_RECIPE_LOGIC.registerForBlockEntity(
                (blockEntity, side) -> getTraitCapability(blockEntity, RecipeLogic.class)
                        .orElse(null),
                type
        );
        GTCapability.CAPABILITY_ENERGY.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, IEnergyContainer.class)
                        .or(() -> getAllTraitCapabilities(blockEntity, IEnergyContainer.class, EnergyContainerList::new))
                        .orElse(null),
                type
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, machine ->
                        Optional.ofNullable(machine.getItemTransferCap(side))
                                .map(ItemTransferHelperImpl::toItemVariantStorage)
                                .orElse(null))
                        .orElse(null),
                type
        );
        FluidStorage.SIDED.registerForBlockEntity(
                (blockEntity, side) -> getMachineCapability(blockEntity, machine ->
                        Optional.ofNullable(machine.getFluidTransferCap(side))
                                .map(FluidTransferHelperImpl::toFluidVariantStorage)
                                .orElse(null))
                        .orElse(null),
                type
        );
    }
}
